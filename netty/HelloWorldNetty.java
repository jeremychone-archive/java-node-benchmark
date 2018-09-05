import static io.netty.buffer.Unpooled.copiedBuffer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.*;
import io.netty.channel.socket.*;
import io.netty.channel.socket.nio.*;
import io.netty.handler.codec.http.*;
import java.util.*;

public class HelloWorldNetty
{
    private static int LOOP_SIZE = 100;

    private ChannelFuture channel;
    private final EventLoopGroup masterGroup;
    private final EventLoopGroup slaveGroup;
    
    public HelloWorldNetty()
    {
        masterGroup = new NioEventLoopGroup();
        slaveGroup = new NioEventLoopGroup();        
    }

    public void start()
    {
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() { shutdown(); }
        });
        
        try
        {
            final ServerBootstrap bootstrap =
                new ServerBootstrap()
                    .group(masterGroup, slaveGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>(){
                        @Override
                        public void initChannel(final SocketChannel ch) throws Exception
                        {
                            ch.pipeline().addLast("codec", new HttpServerCodec());
                            ch.pipeline().addLast("aggregator", new HttpObjectAggregator(512*1024));
                            ch.pipeline().addLast("request", new ChannelInboundHandlerAdapter()
                            {
                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg)
                                        throws Exception
                                {
                                    if (msg instanceof FullHttpRequest)
                                    {
                                        final FullHttpRequest request = (FullHttpRequest) msg;
                                        final Map<String, List<String>> params = (new QueryStringDecoder(request.getUri())).parameters();
                                        final List<String> paramIt = params.get("it");
                                        final int loop_size = (paramIt != null) ? Integer.parseInt(paramIt.get(0)) : LOOP_SIZE;
                                        final StringBuffer sb = new StringBuffer();
                                        for (int i = 0; i < loop_size; i++ ){
                                          sb.append("item ").append(i).append("\n");
                                        }                                        
                                        final String buf = sb.toString();
                                        final String responseMessage = "<h1>Loop Size: " + loop_size + " Buf Size: " + buf.length() + " </h1>";                                           
                                        FullHttpResponse response = new DefaultFullHttpResponse(
                                            HttpVersion.HTTP_1_1,
                                            HttpResponseStatus.OK,
                                            copiedBuffer(responseMessage.getBytes())
                                        );
    
                                        if (HttpHeaders.isKeepAlive(request))
                                        {
                                            response.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                                        }
                                        response.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/plain");
                                        response.headers().set(HttpHeaders.Names.CONTENT_LENGTH, responseMessage.length());
                                        
                                        ctx.writeAndFlush(response);
                                    }
                                    else
                                    {
                                        super.channelRead(ctx, msg);
                                    }
                                }
    
                                @Override
                                public void channelReadComplete(ChannelHandlerContext ctx) throws Exception
                                {
                                    ctx.flush();
                                }
    
                                @Override
                                public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                                        throws Exception
                                {
                  ctx.writeAndFlush(new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.INTERNAL_SERVER_ERROR,
                    copiedBuffer(cause.getMessage().getBytes())
                  ));
                                }                                    
                            });
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            channel = bootstrap.bind(8080).sync();
            //channels.add(bootstrap.bind(8080).sync());
        }
        catch (final InterruptedException e) { }
    }
    
    public void shutdown()
    {
        slaveGroup.shutdownGracefully();
        masterGroup.shutdownGracefully();

        try
        {
            channel.channel().closeFuture().sync();
        }
        catch (InterruptedException e) { }
    }

    public static void main(String[] args)
    {
        new HelloWorldNetty().start();
    }
}