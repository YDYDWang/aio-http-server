package com.github.ydydwang.aio.http.parser;

import java.nio.ByteBuffer;

import com.github.ydydwang.aio.channel.ChannelContext;
import com.github.ydydwang.aio.http.bean.Session;

public interface Parser {

	void parser(String address, ByteBuffer byteBuffer, Session session
			, ChannelContext channelContext) throws Exception;
}
