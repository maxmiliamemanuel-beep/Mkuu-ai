import express from "express";
import { z } from "zod";

const env = z.object({ PORT:z.coerce.number().default(8080), AI_PROVIDER_API_KEY:z.string().min(1), AI_PROVIDER_BASE_URL:z.string().url().default("https://api.openai.com/v1"), AI_CHAT_MODEL:z.string().default("gpt-4.1-mini"), AI_IMAGE_MODEL:z.string().default("gpt-image-1"), ALLOWED_ORIGIN:z.string().url().optional() }).parse(process.env);
const app=express(); app.disable("x-powered-by"); app.use(express.json({limit:"64kb"}));
app.use((req,res,next)=>{ if(env.ALLOWED_ORIGIN) res.setHeader("Access-Control-Allow-Origin",env.ALLOWED_ORIGIN); res.setHeader("Strict-Transport-Security","max-age=31536000; includeSubDomains"); next(); });
const chat=z.object({conversationId:z.string().uuid(),message:z.string().trim().min(1).max(8000),webSearch:z.boolean().default(false)});
const image=z.object({prompt:z.string().trim().min(1).max(2000)});
const providerHeaders={"Authorization":`Bearer ${env.AI_PROVIDER_API_KEY}`,"Content-Type":"application/json"};
app.get("/health",(_,res)=>res.json({status:"ok"}));
app.post("/v1/chat/stream",async(req,res,next)=>{ try { const input=chat.parse(req.body); res.status(200).set({"Content-Type":"text/event-stream","Cache-Control":"no-cache, no-transform","Connection":"keep-alive","X-Accel-Buffering":"no"}); res.flushHeaders();
 const instructions=input.webSearch ? "Use web search if available for time-sensitive claims. Label externally retrieved claims and return source URLs." : "Answer using general knowledge. Do not claim you searched the web.";
 const upstream=await fetch(`${env.AI_PROVIDER_BASE_URL}/chat/completions`,{method:"POST",headers:providerHeaders,body:JSON.stringify({model:env.AI_CHAT_MODEL,stream:true,messages:[{role:"system",content:instructions},{role:"user",content:input.message}]})}); if(!upstream.ok||!upstream.body) throw new Error(`Provider error ${upstream.status}`);
 const reader=upstream.body.getReader(),decoder=new TextDecoder(),encoder=new TextEncoder(); while(true){const {done,value}=await reader.read();if(done)break;for(const line of decoder.decode(value,{stream:true}).split("\n")){if(!line.startsWith("data: "))continue;const raw=line.slice(6);if(raw==="[DONE]")continue;try { const event=JSON.parse(raw); const delta=event.choices?.[0]?.delta?.content; if(delta) res.write(`data: ${delta}\n\n`); }catch{ /* ignore keepalive */ }}} res.write("data: [DONE]\n\n");res.end(); } catch(error){next(error)} });
app.post("/v1/images",async(req,res,next)=>{try{const {prompt}=image.parse(req.body);const r=await fetch(`${env.AI_PROVIDER_BASE_URL}/images/generations`,{method:"POST",headers:providerHeaders,body:JSON.stringify({model:env.AI_IMAGE_MODEL,prompt,size:"1024x1024"})});if(!r.ok)throw new Error(`Provider error ${r.status}`);const body=await r.json() as {data?:{url?:string}[]};const url=body.data?.[0]?.url;if(!url)throw new Error("Provider did not return an image");res.json({url});}catch(error){next(error)}});
app.use((error:unknown,_req:express.Request,res:express.Response,_next:express.NextFunction)=>{const message=error instanceof z.ZodError?"Invalid request":error instanceof Error?error.message:"Unexpected error";res.status(message==="Invalid request"?400:502).json({error:message});});
app.listen(env.PORT,()=>console.log(`MKUU AI backend listening on ${env.PORT}`));
