package org.jire.js5server

import com.mark.Application
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.IdleStateHandler
import org.jire.js5server.PipelineConstants.DECODER
import org.jire.js5server.PipelineConstants.ENCODER
import org.jire.js5server.PipelineConstants.HANDLER
import org.jire.js5server.PipelineConstants.IDLE_STATE_HANDLER
import org.jire.js5server.codec.init.InitDecoder
import org.jire.js5server.codec.init.InitEncoder
import org.jire.js5server.codec.init.InitHandler
import java.io.File
import java.util.concurrent.TimeUnit

class Js5ChannelInitializer(
    private val groupRepository: Js5GroupRepository = Openrs2Js5GroupRepository().apply(Js5GroupRepository::load),

    private val timeout: Long = 30,
    private val timeoutUnit: TimeUnit = TimeUnit.SECONDS
) : ChannelInitializer<SocketChannel>() {

    override fun initChannel(ch: SocketChannel) {
        ch.pipeline().run {
            addLast(
                IDLE_STATE_HANDLER,
                IdleStateHandler(true, 0, 0, timeout, timeoutUnit)
            )

            addLast(DECODER, InitDecoder())
            addLast(ENCODER, InitEncoder())
            //addLast(FLOW_CONTROL_HANDLER, FlowControlHandler())
            addLast(HANDLER, InitHandler(Application.builder.js5Revision, groupRepository))
        }
    }

}