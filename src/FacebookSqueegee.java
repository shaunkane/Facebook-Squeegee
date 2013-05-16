import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.print.attribute.standard.DateTimeAtCompleted;

import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.types.*;

import utils.SaveImage;

public class FacebookSqueegee {
	private static String MY_ACCESS_TOKEN = 
		"CAACEdEose0cBADattyCX6PJ10QeKnMIvZC2XDZBRmywjjsE5anZB4UWLKPWLqGzMAZBjiWQCZCq0yvJAzbQibPjU2pkW01v2NolqgMJ6F5gUvP2sjGfVZBLviJZCvFFFQ6hVT0zLBGCdSxXLibaoV2NhxBCFi5JEXMfZA922EqIOzwZDZD";
	private static String WARBY_PARKER_FB_PAGE = "warbyparker/tagged";
	
	// Output Variables
	private static DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
	private static Date date = new Date();
	private static String OUT_FILE_NAME = "./" + dateFormat.format(date).toString() + ".txt";
	
	public static void main(String[] args) {
		try {
			// Sessions expire. Go here for fresh ones: https://developers.facebook.com/tools/access_token/
			FacebookClient facebookClient = new DefaultFacebookClient(MY_ACCESS_TOKEN);
			FileWriter fw = new FileWriter(OUT_FILE_NAME);
			
			// Pass it a stream input and output.
			squeegee(facebookClient, fw);
			
			// All done!
			fw.close();
		} catch (IOException e) {
			System.err.println("Couldn't create output file!");
			e.printStackTrace();
		}		
	}
	
	private static void squeegee(FacebookClient facebookClient, FileWriter fw) {
		StringBuilder record = new StringBuilder("");
		
		Connection<Post> tagFeed = facebookClient.fetchConnection(WARBY_PARKER_FB_PAGE, Post.class);
		
		// Facebook limits the JSON to 25 hits per "page", so we need to hit them all.
		for(List<Post> tagPage : tagFeed) {
			for(Post tag : tagPage) {
				// All kinds of things are posted to feeds.
				// Right now we're only going to pull for photos.
				record = new StringBuilder("");
				
				if(tag.getType() != null) {
					record.append(tag.getType() + " [" + tag.getId() + "]\t");
					
					System.out.println(tag.getLink());
					record.append(tag.getFrom().getName() + " [" + tag.getFrom().getId() + "]\t");
					
					record.append(squeegeePicture(facebookClient, tag));
					record.append(squeegeeApp(facebookClient, tag));
					record.append(squeegeeLikes(facebookClient, tag));
					record.append(squeegeeComments(facebookClient, tag));
					
					try {
						fw.write(record.toString() + "\n");
						fw.flush();
					} catch (IOException e) {
						System.err.println("Couldn't write record! [" + tag.getId() + "]");
						e.printStackTrace();
					}
				}
			}
		}
	}
	private static StringBuilder squeegeePicture(FacebookClient facebookClient, Post post) {
		StringBuilder pictureRecord = new StringBuilder("");
		
		pictureRecord.append(post.getPicture() + "\t");
		pictureRecord.append(post.getCaption() + "\t");
		
		if(post.getPicture() != null) {
			String imageLocal = "./images/" + post.getId() + ".jpg";
			SaveImage.saveImageFromURL(post.getPicture(), imageLocal);
		}
		
		return pictureRecord;
	}
	private static StringBuilder squeegeeComments(FacebookClient facebookClient, Post post) {
		StringBuilder commentsRecord = new StringBuilder("");
		
		if(post.getComments() != null) {
			commentsRecord.append(post.getComments().getData().size() + ";");
			
			commentsRecord.append("{");
			for(Comment comment : post.getComments().getData()) {
				commentsRecord.append(comment.getFrom().getName() + " [" + comment.getFrom().getId() + "] ");
				commentsRecord.append(comment.getMessage() + " ");
				
				// I found Facebook API ugliness! I'll whip this out if they ever job interview me :P
				if(comment.getLikeCount() > 0) {
					Connection<Post> commentLikes =
						facebookClient.fetchConnection(comment.getId() + "/likes", Post.class);
					StringBuilder commentLikesRecord = new StringBuilder("[" + comment.getLikeCount() + "];");
					
					commentsRecord.append("{");
					for(NamedFacebookType commentLike : commentLikes.getData()) {
						commentLikesRecord.append(commentLike.getName() + " [" + commentLike.getId() + "]");
						commentLikesRecord.append(";");
					}
					commentsRecord.append("}");
				} else {
					commentsRecord.append("0;{}");
				}
				
				commentsRecord.append(";");
			}
			commentsRecord.append("}");
		} else {
			commentsRecord.append("0;{}");
		}
		
		return commentsRecord;
	}
	private static StringBuilder squeegeeApp(FacebookClient facebookClient, Post post) {
		StringBuilder appRecord = new StringBuilder("");
		
		if(post.getApplication() != null) {
			appRecord.append(post.getApplication().getName() + " [" + post.getApplication().getId() + "]");
		} else {
			appRecord.append("null");
		}
		
		return appRecord;
	}
	private static StringBuilder squeegeeLikes(FacebookClient facebookClient, Post post) {
		StringBuilder likesRecord = new StringBuilder("");
		
		if(post.getLikes() != null) {
			likesRecord.append(post.getLikesCount() + ";");
			
			likesRecord.append("{");
			for(NamedFacebookType like : post.getLikes().getData()) {
				likesRecord.append(like.getName() + " [" + like.getId() + "]");
				likesRecord.append(";");
			}
			likesRecord.append("}");
		} else {
			likesRecord.append("0;{}");
		}
		
		return likesRecord;
	}
}
