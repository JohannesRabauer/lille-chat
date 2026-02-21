This should be a comparison between GitHub Copilot and Intellij Junie

# Measurements
## GitHub Copilot (online)
1. Time to working build
    - Time started: 20:18(Sonnet) / 20:23(Opus)
    - Time finished: 20:37 -> 14min
2. Number of iterations required
3. Additional input needed?
Error #1 (404 Not Found from GET http://backend:8080/api/sse/subscribe)
4. Tests pass?
yes
5. Code quality and structure?
- It executed some security with GraphQL

## JetBrains Junie (local)
1. Time to working build
    - Time started: 20:22 (Opus)
    - Time finished: 20:29 -> 7min
2. Number of iterations required
3. Additional input needed?
Error #1 (404 Not Found from GET http://backend:8080/api/sse/subscribe)
4. Tests pass? 
yes
5. Code quality and structure?

## Error
Both have the same error:
Now i have the following error if i want to find a friend:
```
2026-02-21T19:50:04.333Z  WARN 1 --- [frontend] [r-http-epoll-19] d.r.l.frontend.service.SseClientService  : SSE stream error
lille-chat-frontend  | 
lille-chat-frontend  | org.springframework.web.reactive.function.client.WebClientResponseException$NotFound: 404 Not Found from GET http://backend:8080/api/sse/subscribe
lille-chat-frontend  |  at org.springframework.web.reactive.function.client.WebClientResponseException.create(WebClientResponseException.java:310) ~[spring-webflux-7.0.3.jar!/:7.0.3]
lille-chat-frontend  |  Suppressed: The stacktrace has been enhanced by Reactor, refer to additional information below: 
lille-chat-frontend  | Error has been observed at the following site(s):
lille-chat-frontend  |  *__checkpoint ⇢ 404 NOT_FOUND from GET http://backend:8080/api/sse/subscribe [DefaultWebClient]
lille-chat-frontend  | Original Stack Trace:
lille-chat-frontend  |          at org.springframework.web.reactive.function.client.WebClientResponseException.create(WebClientResponseException.java:310) ~[spring-webflux-7.0.3.jar!/:7.0.3]
lille-chat-frontend  |          at org.springframework.web.reactive.function.client.DefaultClientResponse.lambda$createException$1(DefaultClientResponse.java:214) ~[spring-webflux-7.0.3.jar!/:7.0.3]
lille-chat-frontend  |          at reactor.core.publisher.FluxMap$MapSubscriber.onNext(FluxMap.java:106) ~[reactor-core-3.8.2.jar!/:3.8.2]
lille-chat-frontend  |          at reactor.core.publisher.FluxOnErrorReturn$ReturnSubscriber.onNext(FluxOnErrorReturn.java:159) ~[reactor-core-3.8.2.jar!/:3.8.2]
lille-chat-frontend  |          at reactor.core.publisher.FluxDefaultIfEmpty$DefaultIfEmptySubscriber.onNext(FluxDefaultIfEmpty.java:123) ~[reactor-core-3.8.2.jar!/:3.8.2]
lille-chat-frontend  |          at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onNext(FluxMapFuseable.java:130) ~[reactor-core-3.8.2.jar!/:3.8.2]
lille-chat-frontend  |          at reactor.core.publisher.FluxContextWrite$ContextWriteSubscriber.onNext(FluxContextWrite.java:109) ~[reactor-core-3.8.2.jar!/:3.8.2]
lille-chat-frontend  |          at reactor.core.publisher.FluxMapFuseable$MapFuseableConditionalSubscriber.onNext(FluxMapFuseable.java:300) ~[reactor-core-3.8.2.jar!/:3.8.2]
lille-chat-frontend  |          at reactor.core.publisher.FluxFilterFuseable$FilterFuseableConditionalSubscriber.onNext(FluxFilterFuseable.java:338) ~[reactor-core-3.8.2.jar!/:3.8.2]
lille-chat-frontend  |          at reactor.core.publisher.Operators$BaseFluxToMonoOperator.completePossiblyEmpty(Operators.java:2093) ~[reactor-core-3.8.2.jar!/:3.8.2]
lille-chat-frontend  |          at reactor.core.publisher.MonoCollect$CollectSubscriber.onComplete(MonoCollect.java:144) ~[reactor-core-3.8.2.jar!/:3.8.2]
lille-chat-frontend  |          at reactor.core.publisher.FluxMap$MapSubscriber.onComplete(FluxMap.java:144) ~[reactor-core-3.8.2.jar!/:3.8.2]
lille-chat-frontend  |          at reactor.core.publisher.FluxPeek$PeekSubscriber.onComplete(FluxPeek.java:263) ~[reactor-core-3.8.2.jar!/:3.8.2]
lille-chat-frontend  |          at reactor.core.publisher.FluxMap$MapSubscriber.onComplete(FluxMap.java:144) ~[reactor-core-3.8.2.jar!/:3.8.2]
lille-chat-frontend  |          at reactor.netty.channel.FluxReceive.onInboundComplete(FluxReceive.java:419) ~[reactor-netty-core-1.3.2.jar!/:1.3.2]
lille-chat-frontend  |          at reactor.netty.channel.ChannelOperations.onInboundComplete(ChannelOperations.java:465) ~[reactor-netty-core-1.3.2.jar!/:1.3.2]
lille-chat-frontend  |          at reactor.netty.channel.ChannelOperations.terminate(ChannelOperations.java:519) ~[reactor-netty-core-1.3.2.jar!/:1.3.2]
lille-chat-frontend  |          at reactor.netty.http.client.HttpClientOperations.onInboundNext(HttpClientOperations.java:956) ~[reactor-netty-http-1.3.2.jar!/:1.3.2]
lille-chat-frontend  |          at reactor.netty.channel.ChannelOperationsHandler.channelRead(ChannelOperationsHandler.java:115) ~[reactor-netty-core-1.3.2.jar!/:1.3.2]
lille-chat-frontend  |          at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:356) ~[netty-transport-4.2.9.Final.jar!/:4.2.9.Final]
lille-chat-frontend  |          at io.netty.handler.codec.http.HttpContentDecoder.decode(HttpContentDecoder.java:170) ~[netty-codec-http-4.2.9.Final.jar!/:4.2.9.Final]
lille-chat-frontend  |          at io.netty.handler.codec.http.HttpContentDecoder.decode(HttpContentDecoder.java:48) ~[netty-codec-http-4.2.9.Final.jar!/:4.2.9.Final]
lille-chat-frontend  |          at io.netty.handler.codec.MessageToMessageDecoder.channelRead(MessageToMessageDecoder.java:91) ~[netty-codec-base-4.2.9.Final.jar!/:4.2.9.Final]
lille-chat-frontend  |          at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:356) ~[netty-transport-4.2.9.Final.jar!/:4.2.9.Final]
lille-chat-frontend  |          at io.netty.channel.CombinedChannelDuplexHandler$DelegatingChannelHandlerContext.fireChannelRead(CombinedChannelDuplexHandler.java:434) ~[netty-transport-4.2.9.Final.jar!/:4.2.9.Final]
lille-chat-frontend  |          at io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:361) ~[netty-codec-base-4.2.9.Final.jar!/:4.2.9.Final]
lille-chat-frontend  |          at io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:325) ~[netty-codec-base-4.2.9.Final.jar!/:4.2.9.Final]
lille-chat-frontend  |          at io.netty.channel.CombinedChannelDuplexHandler.channelRead(CombinedChannelDuplexHandler.java:249) ~[netty-transport-4.2.9.Final.jar!/:4.2.9.Final]
lille-chat-frontend  |          at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:354) ~[netty-transport-4.2.9.Final.jar!/:4.2.9.Final]
lille-chat-frontend  |          at io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1429) ~[netty-transport-4.2.9.Final.jar!/:4.2.9.Final]
lille-chat-frontend  |          at io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:918) ~[netty-transport-4.2.9.Final.jar!/:4.2.9.Final]
lille-chat-frontend  |          at io.netty.channel.epoll.AbstractEpollStreamChannel$EpollStreamUnsafe.epollInReady(AbstractEpollStreamChannel.java:804) ~[netty-transport-classes-epoll-4.2.9.Final.jar!/:4.2.9.Final]
lille-chat-frontend  |          at io.netty.channel.epoll.AbstractEpollChannel$AbstractEpollUnsafe.handle(AbstractEpollChannel.java:482) ~[netty-transport-classes-epoll-4.2.9.Final.jar!/:4.2.9.Final]
lille-chat-frontend  |          at io.netty.channel.epoll.EpollIoHandler$DefaultEpollIoRegistration.handle(EpollIoHandler.java:317) ~[netty-transport-classes-epoll-4.2.9.Final.jar!/:4.2.9.Final]
lille-chat-frontend  |          at io.netty.channel.epoll.EpollIoHandler.processReady(EpollIoHandler.java:515) ~[netty-transport-classes-epoll-4.2.9.Final.jar!/:4.2.9.Final]
lille-chat-frontend  |          at io.netty.channel.epoll.EpollIoHandler.run(EpollIoHandler.java:460) ~[netty-transport-classes-epoll-4.2.9.Final.jar!/:4.2.9.Final]
lille-chat-frontend  |          at io.netty.channel.SingleThreadIoEventLoop.runIo(SingleThreadIoEventLoop.java:225) ~[netty-transport-4.2.9.Final.jar!/:4.2.9.Final]
lille-chat-frontend  |          at io.netty.channel.SingleThreadIoEventLoop.run(SingleThreadIoEventLoop.java:196) ~[netty-transport-4.2.9.Final.jar!/:4.2.9.Final]
lille-chat-frontend  |          at io.netty.util.concurrent.SingleThreadEventExecutor$5.run(SingleThreadEventExecutor.java:1195) ~[netty-common-4.2.9.Final.jar!/:4.2.9.Final]
lille-chat-frontend  |          at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) ~[netty-common-4.2.9.Final.jar!/:4.2.9.Final]
lille-chat-frontend  |          at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) ~[netty-common-4.2.9.Final.jar!/:4.2.9.Final]
lille-chat-frontend  |          at java.base/java.lang.Thread.run(Unknown Source) ~[na:na]
lille-chat-frontend  | 
lille-chat-frontend  | 2026-02-21T19:50:11.705Z  WARN 1 --- [frontend] [r-http-epoll-16] d.r.l.frontend.service.SseClientService  : SSE stream error
lille-chat-frontend  | 
lille-chat-frontend  | org.springframework.web.reactive.function.client.WebClientResponseException$NotFound: 404 Not Found from GET http://backend:8080/api/sse/subscribe
```
