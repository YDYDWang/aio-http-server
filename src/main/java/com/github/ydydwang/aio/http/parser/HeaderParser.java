package com.github.ydydwang.aio.http.parser;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import com.github.ydydwang.aio.channel.ChannelContext;
import com.github.ydydwang.aio.common.Numbers;
import com.github.ydydwang.aio.http.bean.Session;

public class HeaderParser extends AbstractParser {
	public static final Parser instance = new HeaderParser();

	@Override
	public void parser(String address, ByteBuffer byteBuffer, Session session
			, ChannelContext ctx) throws Exception {
		int readerIndex = Numbers.INT_ZERO;
		boolean stillHeader = Boolean.TRUE;
		loop : for (int i = Numbers.INT_ZERO; i < byteBuffer.readableBytes(); i++) {
			switch (byteBuffer.getByte(i)) {
				case CompleteType.LF:
					ByteBuffer header = byteBuffer.slice(readerIndex, i - readerIndex - Numbers.INTEGER_ONE);
					readerIndex = ++i;
					if (handleHeader(header, session) == Boolean.FALSE) {
						stillHeader = Boolean.FALSE;
						break loop;
					}
				break;
			}
		}
		if (stillHeader) {
			if (readerIndex < byteBuffer.readableBytes()) {
				ByteBuffer slice = byteBuffer.slice(readerIndex, byteBuffer.readableBytes() - readerIndex);
				session.addSlice(slice);
			}
		} else if (session.getContentLength() > Numbers.INT_ZERO) {
			session.setParser(ContentParser.instance);
			if (readerIndex < byteBuffer.readableBytes()) {
				ByteBuffer slice = byteBuffer.slice(readerIndex, byteBuffer.readableBytes() - readerIndex);
				session.getParser().parser(address, slice, session, ctx);
			}
		}
	}

	public static boolean validate(Session session) {
		if (session.getMethod() == null
				|| session.getUri() == null
				|| session.getContentType() == null) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	public static void extracHeaders(Session session) {
		extracFirstLine(session);
		for (int i = Numbers.INT_ONE; i < session.getHeaderList().size(); i++) {
			ByteBuffer header = session.getHeaderList().get(i);
			if (startWith(header, contentLength)) {
				int skipLength = skip(header, contentLength.readableBytes());
				int from = contentLength.readableBytes() + skipLength;
				int contentLength = Integer.valueOf(header.slice(from, header.readableBytes() - from).toString(StandardCharsets.UTF_8));
				session.setContentLength(contentLength);
			} else if (startWith(header, contentType)) {
				int skipLength = skip(header, contentType.readableBytes());
				int from = contentLength.readableBytes() + skipLength;
				if (startWith(header, text, from)) {
					session.setContentType(ContentType.TEXT);;
				} else if (startWith(header, multipart, from)) {
					session.setContentType(ContentType.MUTIPART);;
				} else if (startWith(header, application, from)) {
					session.setContentType(ContentType.X_WWW_FORM_URLENCODED);;
				}
			}
		}
	}

	public static int skip(ByteBuffer byteBuffer, int from) {
		int length = Numbers.INT_ZERO;
		for (int i = from; i < byteBuffer.readableBytes(); i++) {
			byte b = byteBuffer.getByte(i);
			if (b == ':' || b == ' ') {
				length++;
			} else {
				break;
			}
		}
		return length;
	}

	public static boolean startWith(ByteBuffer header, ByteBuffer prefix) {
		if (header.readableBytes() > prefix.readableBytes()) {
			for (int i = Numbers.INT_ZERO; i < prefix.readableBytes(); i++) {
				if (prefix.getByte(i) != header.getByte(i)) {
					return Boolean.FALSE;
				}
			}
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	public static boolean startWith(ByteBuffer header, ByteBuffer prefix, int from) {
		// FIXME check it is better if ((header.readableBytes() - from) > prefix.readableBytes()) {
		if (header.readableBytes() > prefix.readableBytes()) {
			for (int i = from; i < prefix.readableBytes(); i++) {
				if (prefix.getByte(i) != header.getByte(i)) {
					return Boolean.FALSE;
				}
			}
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	public static void extracFirstLine(Session session) {
		ByteBuffer header = session.getHeaderList().get(Numbers.INT_ZERO);
		if (header.getByte(0) == 'G' && header.getByte(1) == 'E' && header.getByte(2) == 'T') {
			session.setMethod(Method.GET);
			extracUri(header, session, 4);
		} else if (header.getByte(0) == 'P' && header.getByte(1) == 'O' && header.getByte(2) == 'S' && header.getByte(3) == 'T') {
			session.setMethod(Method.POST);
			extracUri(header, session, 5);
		}
	}

	public static void extracUri(ByteBuffer header, Session session, int from) {
		for (int i = from + Numbers.INTEGER_ONE; i < header.readableBytes(); i++) {
			switch (header.getByte(i)) {
				case ' ':
					ByteBuffer uri = header.slice(from, i - from);
					session.setUri(uri);
					i = header.readableBytes();
				break;
			}
		}
	}

	public static boolean handleHeader(ByteBuffer header, Session session) {
		if (isNotEmpty(header)) {
			addToHeaderList(session, header);
			return Boolean.TRUE;
		}
		extracHeaders(session);
		if (validate(session)) {
			// TODO RESPONSE HEADER MISSING
		}
		return Boolean.FALSE;
	}

	public static void addToHeaderList(Session session, ByteBuffer header) {
		if (CollectionUtils.isNotEmpty(session.getSliceList())) {
			CompositeByteBuffer compositeByteBuffer = PooledByteBufferAllocator.DEFAULT
					.compositeBuffer(session.getSliceList().size() + Numbers.INTEGER_ONE);
			for (ByteBuffer byteBuffer : session.getSliceList()) {
				compositeByteBuffer.addComponent(byteBuffer);
			}
			session.addHeader(compositeByteBuffer);
			session.releaseSliceList();
		} else {
			session.addHeader(header);
		}
	}
}
