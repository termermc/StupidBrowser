package net.termer.stupidbrowser;

import java.io.IOException;
import java.io.InputStream;

public class ResourceLoader {
	public static String getInternalWebpage(String path) throws IOException {
		String tmp = "";
		path=path.trim();
		if(path.startsWith("/")) path=path.substring(1);
		if(path.length()<1) path="about";
		@SuppressWarnings("rawtypes")
		Class c=null;
		try {
	    	c = Browser.class;
	    } catch (Exception ex) {
	    	ex.printStackTrace();
	    }
		InputStream s = c.getResourceAsStream("/resources/webpages/"+path+".html");
		if(s==null) {
			throw new IOException("Webpage "+path+" not found");
		} else {
			while(s.available()>0) {
				tmp+=(char)s.read();
			}
			s.close();
		}
		if(tmp.contains("%bookmarks")) {
			String bookmarks = "";
			String[] titles = Browser._BOOKMARKS_.keySet().toArray(new String[0]);
			String[] urls = Browser._BOOKMARKS_.values().toArray(new String[0]);
			for(int i = 0; i < titles.length; i++) {
				if(bookmarks.length()>0) bookmarks+="<br><br>";
				bookmarks+="<span class=\"bookmark\"><a href=\""+urls[i]+"\">"+titles[i]+"</a> </span> ";
				bookmarks+="<span class=\"bookmark-actions\"><a href=\"action:edit_bookmark:"+titles[i]+"///"+urls[i]+"\">[edit]</a> ";
				bookmarks+="<a href=\"action:delete_bookmark:"+titles[i]+"\">[delete]</a></span>";
			}
			if(bookmarks.length()<1) {
				bookmarks = "<i>No bookmarks...</i>";
			}
			tmp = tmp.replaceAll("%bookmarks", bookmarks);
		}
		return tmp
				.replaceAll("%name", Browser._NAME_)
				.replaceAll("%homepage", Browser._HOMEPAGE_);
	}
}