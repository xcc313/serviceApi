package com.lzj.op;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.lzj.utils.ImageMessage;
import com.lzj.utils.MessageItem;

/**
 * 消息基类（普通用户 -> 公众帐号）
 * @author Administrator
 *
 */
@XmlRootElement
public class TextMessage {
	// 开发者微信号(平台号)
	private String FromUserName;
	// 发送方帐号（一个OpenID）
	private String ToUserName;
	//用户昵称
	private String userName;
	// 消息创建时间 （整型）
	private long CreateTime;
	// 消息类型（text/image/location/link/news）
	private String MsgType;
	// 消息内容
	private String Content;
	//图文消息个数，限制为10条以内
	private String ArticleCount;
	//多条图文消息信息，默认第一个item为大图,注意，如果图文数超过10，则将会无响应
	//private Articles Articles;
	@XmlElementWrapper(name="Articles")
    @XmlElement(name="item")
	private List<MessageItem> Articles;
	//被动回复图片消息
	@XmlElementWrapper(name="Image")
	private ImageMessage Image;
	


	public long getCreateTime() {
		return CreateTime;
	}

	public void setCreateTime(long createTime) {
		CreateTime = createTime;
	}

	public String getMsgType() {
		return MsgType;
	}

	public void setMsgType(String msgType) {
		MsgType = msgType;
	}

	public String getContent() {
		return Content;
	}

	public void setContent(String content) {
		Content = content;
	}


	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getFromUserName() {
		return FromUserName;
	}

	public void setFromUserName(String fromUserName) {
		FromUserName = fromUserName;
	}

	public String getToUserName() {
		return ToUserName;
	}

	public void setToUserName(String toUserName) {
		ToUserName = toUserName;
	}

	public String getArticleCount() {
		return ArticleCount;
	}

	public void setArticleCount(String articleCount) {
		ArticleCount = articleCount;
	}

	public List<MessageItem> getArticles() {
		return Articles;
	}

	public void setArticles(List<MessageItem> articles) {
		Articles = articles;
	}

	public ImageMessage getImage() {
		return Image;
	}

	public void setImage(ImageMessage image) {
		Image = image;
	}

	/*@XmlElementWrapper(name="books")  
    @XmlElement(name="book")*/
	/*public List<MessageItem> getArticles() {
		return Articles;
	}

	public void setArticles(List<MessageItem> articles) {
		Articles = articles;
	}*/
	
	/*@XmlType(namespace = "Articles")
	@XmlAccessorType(value = XmlAccessType.PROPERTY)
	public static class Articles{
		private List<MessageItem> item;

		public List<MessageItem> getItem() {
			return item;
		}

		public void setItem(List<MessageItem> item) {
			this.item = item;
		}


	}
 
	public Articles getArticles() {
		return Articles;
	}

	public void setArticles(Articles articles) {
		Articles = articles;
	}
	*/
	
}

