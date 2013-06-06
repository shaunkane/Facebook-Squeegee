import java.io.FileWriter;
import java.io.IOException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.types.*;

import utils.SaveImage;

public class FacebookSqueegee {
	private static String MY_ACCESS_TOKEN = 
		"CAACEdEose0cBAEbvbSIQZAXeuOAOLiwrZAgb4lT0rD6B11nPEWQr7eehKc3gZCxW2CuMRR0bhZCAOePQCD6zSxbNQt15aeavxzNjd2Pi3qYnZCjHeHZCIqEerUmrGwWzQfZCHyzZAKKMbvNbQZBESG7tiSkNid6QZAIKcncopiZBfrZCPwZDZD";
	private static String WARBY_PARKER_FB_PAGE = "warbyparker/tagged";
	private static String WARBY_PARKER_FB_ID = "308998183837";
	private static DateFormat ISO_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static Date DATE = new Date();
	private static String OUT_MAIN_FILE_NAME = "./" + ISO_DATE_FORMAT.format(DATE).toString() + ".txt";
	private static String WHITE_SPACE_STRIP = "[\t\n\r\f]";
	
	public static void main(String[] args) {
		// App Start
		System.out.println("Getting started at: " + ISO_DATE_FORMAT.format(DATE).toString());
		
		try {
			// Sessions expire. Go here for fresh ones: https://developers.facebook.com/tools/access_token/
			FacebookClient facebookClient = new DefaultFacebookClient(MY_ACCESS_TOKEN);
			
			// The big dump file.
			FileWriter fw = new FileWriter(OUT_MAIN_FILE_NAME);
			
			// Pass the squeegee a stream for input and output.
			squeegee(facebookClient, fw);
			
			// All done!
			fw.close();
		} catch (IOException e) {
			System.err.println("Couldn't create output file(s)!");
			e.printStackTrace();
		}		
		
		// App End
		Date end = new Date();
		System.out.println("Wrapping up at: " + ISO_DATE_FORMAT.format(end).toString());
	}
	
	private static void squeegee(FacebookClient facebookClient, FileWriter fw) {
		StringBuilder record = new StringBuilder("");
		
		Connection<Post> tagFeed = facebookClient.fetchConnection(WARBY_PARKER_FB_PAGE, Post.class);
		
		int counter = 0;
		
		// Facebook limits the JSON to 25 hits per "page", so we need to hit them all.
		for(List<Post> tagPage : tagFeed) {
			for(Post tag : tagPage) {
				// All kinds of things are posted to feeds.
				// Right now we're only going to pull for photos.
				record = new StringBuilder("");
				
				// I should never get a null Post type.
				if(tag.getType() != null) {
					record.append(tag.getType() + "\t");
					record.append(tag.getId() + "\t");
					record.append(ISO_DATE_FORMAT.format(tag.getCreatedTime()).toString() + "\t");
					record.append(tag.getFrom().getName() + "\t"); 
					record.append(tag.getFrom().getId() + "\t");
					
					// Most of these aren't "required" in the Post type, 
					// so I have to do some additional validation
					// before I "get" the values or write a null.
					// Hence the special methods.
					record.append(squeegeePicture(tag) + "\t");
					record.append(squeegeeCaption(tag) + "\t");
					record.append(squeegeeMessage(tag) + "\t");
					record.append(squeegeeApp(tag) + "\t");
					record.append(squeegeeLikeCount(tag) + "\t");
					record.append(squeegeeCommentCount(tag) + "\n");
					
					// Just so I can follow along in the console.
					System.out.println("[" + String.format("%05d", counter) + "]\t" 
							+ tag.getType() + "\t" + tag.getId());
					counter++;
					
					// Likes and comments are stored as separate records.
					record.append(squeegeeLikes(tag));
					record.append(squeegeeComments(tag));
					
					try {
						fw.write(record.toString());
						fw.flush();						
					} catch (IOException e) {
						System.err.println("Couldn't write record! [" + tag.getId() + "]");
						e.printStackTrace();
					}
				} else {
					System.err.println("Null! [" + tag.getId() + "]");
				}
			}
		}
	}
	private static StringBuilder squeegeePicture(Post post) {
		StringBuilder pictureRecord = new StringBuilder("");
		
		// This avoids all those annoying place-holder images.
		if(post.getType().equalsIgnoreCase("photo") && post.getPicture() != null) {
			String imageLocal = "./images/" + post.getId() + ".jpg";
			
			if(post.getSource() != null) {
				pictureRecord.append(post.getSource());
				
				SaveImage.saveImageFromURL(post.getSource(), imageLocal);
			} else {
				pictureRecord.append(post.getPicture());
				
				SaveImage.saveImageFromURL(post.getPicture(), imageLocal);
			}
		} else {
			pictureRecord.append("null");
		}
		
		return pictureRecord;
	}
	private static StringBuilder squeegeeCaption(Post post) {
		StringBuilder captionRecord = new StringBuilder("");
		
		if(post.getCaption() != null) {
			captionRecord.append(post.getCaption().replaceAll(WHITE_SPACE_STRIP, " "));
		} else {
			captionRecord.append("null");
		}
		
		return captionRecord;
	}
	private static StringBuilder squeegeeMessage(Post post) {
		StringBuilder messageRecord = new StringBuilder("");
		
		if(post.getMessage() != null) {
			messageRecord.append(post.getMessage().replaceAll(WHITE_SPACE_STRIP, " "));
		} else {
			messageRecord.append("null");
		}
		
		return messageRecord;
	}	
	private static StringBuilder squeegeeApp(Post post) {
		StringBuilder appRecord = new StringBuilder("");
		
		if(post.getApplication() != null) {
			appRecord.append(post.getApplication().getName() + " [" + post.getApplication().getId() + "]");
		} else {
			appRecord.append("null");
		}

		return appRecord;
	}
	private static StringBuilder squeegeeLikeCount(Post post) {
		StringBuilder likeCountRecord = new StringBuilder("");
		
		if(post.getLikes() != null) {
			likeCountRecord.append(post.getLikesCount());	
		} else {
			likeCountRecord.append("0");
		}
		
		return likeCountRecord;
	}
	private static StringBuilder squeegeeCommentCount(Post post) {
		StringBuilder commentCountRecord = new StringBuilder("");
		
		if(post.getComments() != null) {
			commentCountRecord.append(post.getComments().getData().size());	
		} else {
			commentCountRecord.append("0");
		}
		
		return commentCountRecord;
	}
	private static StringBuilder squeegeeLikes(Post post) {
		// Likes are like records just with a lot less information, so we start a new "row".
		StringBuilder likesRecord = new StringBuilder("");
		
		if(post.getLikes() != null) {
			for(NamedFacebookType like : post.getLikes().getData()) {
				likesRecord.append("like" + "\t");
				likesRecord.append(post.getId() + "\t");
				likesRecord.append("null" + "\t");
				likesRecord.append(like.getName() + "\t");
				likesRecord.append(like.getId() + "\t");
				
				likesRecord.append("null" + "\t");
				likesRecord.append("null" + "\t");
				likesRecord.append("null" + "\t");
				likesRecord.append("null" + "\t");
				likesRecord.append("0" + "\t");
				likesRecord.append("0" + "\n");
				
				// Likes can't have comments or likes.
			}
		}
		
		return likesRecord;
	}
 	private static StringBuilder squeegeeComments(Post post) {
		StringBuilder commentsRecord = new StringBuilder("");
		
		if(post.getComments() != null) {
			for(Comment comment : post.getComments().getData()) {
				commentsRecord.append("comment" + "\t");
				commentsRecord.append(post.getId() + "\t");
				commentsRecord.append(ISO_DATE_FORMAT.format(comment.getCreatedTime()) + "\t");
				commentsRecord.append(comment.getFrom().getName() + "\t");
				commentsRecord.append(comment.getFrom().getId() + "\t");
				commentsRecord.append("null" + "\t");
				commentsRecord.append("null" + "\t");
				if(comment.getMessage() != null) {
					commentsRecord.append(comment.getMessage().replaceAll(WHITE_SPACE_STRIP, " ") + "\t");	
				} else {
					commentsRecord.append("null" + "\t");
				}
				commentsRecord.append("null" + "\t");
				commentsRecord.append(comment.getLikeCount() + "\t");
				commentsRecord.append("0" + "\n");
				
				// Comments are not threaded.
				// The Facebook API does not expose deeper info on comment likes... Lame.
			}
		}
			
		return commentsRecord;
	}
}
