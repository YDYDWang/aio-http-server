package com.github.ydydwang.aio.http.bean;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.github.ydydwang.aio.http.parser.Parser;
import com.github.ydydwang.aio.util.ByteBufferUtils;
import com.github.ydydwang.http.parser.HeaderParser;

public class Session {

	private Method method;
	private ByteBuffer uri;
	private int contentLength;
	private ContentType contentType;
	private List<ByteBuffer> byteBufList = new ArrayList<ByteBuffer>();
	private List<ByteBuffer> headerList = new ArrayList<ByteBuffer>();
	private List<ByteBuffer> sliceList = new ArrayList<ByteBuffer>();
	private ByteBuffer content;
	private boolean releaseable = Boolean.TRUE;
	private int contentLengthReceived;
	private Parser parser = HeaderParser.instance;

	public Method getMethod() {
		return method;
	}
	public void setMethod(Method method) {
		this.method = method;
	}
	public ByteBuffer getUri() {
		return uri;
	}
	public void setUri(ByteBuffer uri) {
		this.uri = uri;
	}
	public int getContentLength() {
		return contentLength;
	}
	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}
	public ContentType getContentType() {
		return contentType;
	}
	public void setContentType(ContentType contentType) {
		this.contentType = contentType;
	}
	public List<ByteBuffer> getByteBufferList() {
		return byteBufList;
	}
	public void addByteBuffer(ByteBuffer byteBuf) {
		this.byteBufList.add(byteBuf);
	}
	public List<ByteBuffer> getHeaderList() {
		return headerList;
	}
	public void addHeader(ByteBuffer byteBuf) {
		this.headerList.add(byteBuf);
	}
	public List<ByteBuffer> getSliceList() {
		return sliceList;
	}
	public void newSliceList() {
		sliceList = new ArrayList<ByteBuffer>();
	}
	public void addSlice(ByteBuffer byteBuf) {
		this.sliceList.add(byteBuf);
	}
	public ByteBuffer getContent() {
		return content;
	}
	public void setContent(ByteBuffer content) {
		this.content = content;
	}
	public int getContentLengthReceived() {
		return contentLengthReceived;
	}
	public void plusContentLengthReceived(int contentLengthReceived) {
		this.contentLengthReceived += contentLengthReceived;
	}
	public Parser getParser() {
		return parser;
	}
	public void setParser(Parser parser) {
		this.parser = parser;
	}
	public boolean isReleaseable() {
		if (this.releaseable) {
			synchronized(this) {
				if (this.releaseable) {
					this.releaseable = Boolean.FALSE;
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}
	public void releaseQietly() {
		releaseUri();
		releaseSliceList();
		releaseHeaderList();
		releaseContent();
		releaseByteBufferList();
	}
	public void releaseUri() {
		if (uri != null) {
			releaseQietly(uri);
		}
	}
	public void releaseByteBufferList() {
		for (int i = byteBufList.size() - 1; i >= NumberUtils.INTEGER_ZERO; i--) {
			releaseQietly(byteBufList.get(i));
		}
		byteBufList.clear();
	}
	public void releaseHeaderList() {
		for (int i = headerList.size() - 1; i >= NumberUtils.INTEGER_ZERO; i--) {
			releaseQietly(headerList.get(i));
		}
		headerList.clear();
	}
	public void releaseSliceList() {
		for (int i = sliceList.size() - Numbers.INT_ONE; i >= Numbers.INT_ZERO; i--) {
			ByteBufferUtils.releaseQuietly(sliceList.get(i));
		}
		sliceList.clear();
	}
	public void releaseContent() {
		if (content != null) {
			ByteBufferUtils.releaseQuietly(content);
		}
	}
}