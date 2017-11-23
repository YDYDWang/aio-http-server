package com.github.ydydwang.aio.http.parser;

import java.nio.ByteBuffer;

import com.github.ydydwang.aio.common.Numbers;
import com.github.ydydwang.aio.http.bean.Session;
import com.github.ydydwang.aio.util.ByteBufferUtils;
import com.github.ydydwang.aio.util.CollectionUtils;

public abstract class AbstractParser implements Parser {
	protected static final ByteBuffer contentLength = ByteBufferUtils.toByteBuffer("Content-Length");
	protected static final ByteBuffer contentType = ByteBufferUtils.toByteBuffer("Content-Type");
	protected static final ByteBuffer multipart = ByteBufferUtils.toByteBuffer("multipart");
	protected static final ByteBuffer text = ByteBufferUtils.toByteBuffer("text");
	protected static final ByteBuffer application = ByteBufferUtils.toByteBuffer("application");
	protected static final ByteBuffer space = ByteBufferUtils.toByteBuffer(" ");
	protected static final ByteBuffer colon = ByteBufferUtils.toByteBuffer(":");
	protected static final ByteBuffer semicolon = ByteBufferUtils.toByteBuffer(";");

	public static boolean isNotEmpty(ByteBuffer byteBuffer) {
		return byteBuffer.duplicate()
				.rewind()
				.hasRemaining();
	}

	public static void addToContent(Session session, ByteBuffer content) {
		if (CollectionUtils.isNotEmpty(session.getSliceList())) {
			CompositeByteBuffer compositeByteBuffer = PooledByteBufferAllocator.DEFAULT
					.compositeBuffer(session.getSliceList().size() + Numbers.INT_ONE);
			for (ByteBuffer byteBuf : session.getSliceList()) {
				compositeByteBuffer.addComponent(byteBuf);
			}
			session.setContent(compositeByteBuffer);
			session.releaseSliceList();
		} else {
			session.setContent(content);
		}
	}
}
