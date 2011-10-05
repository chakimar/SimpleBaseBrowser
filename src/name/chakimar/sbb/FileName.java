package name.chakimar.sbb;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileName {

	private static final int MAX_COUNT = 100;
	private String fileName;
	private String name;
	private String extension;

	public FileName(String fileName) {
		this.fileName = fileName;
	    int point = fileName.lastIndexOf(".");
	    this.name = fileName.substring(0, point);
	    this.extension = fileName.substring(point + 1);
	    
	}
	
	public String nextFileName() {
		String nextName = "";

	    Pattern pattern = Pattern.compile("-\\d*$");
	    Matcher matcher = pattern.matcher(name);
	    if (matcher.find()) {
	    	int start = matcher.start();
	    	int end = matcher.end();
	    	String countStr = name.substring(start + 1, end);
	    	int count = Integer.parseInt(countStr);
	    	count++;
	    	
	    	nextName = matcher.replaceFirst("-" + count);
	    } else {
	    	nextName = name + "-1";
	    }
	    
		
		return nextName + "." + extension;
	}
	
	public String getFileNameByCount(int count) {
		String countedName = "";
	    Pattern pattern = Pattern.compile("-\\d*$");
	    Matcher matcher = pattern.matcher(name);
	    if (matcher.find()) {
	    	countedName = matcher.replaceFirst("-" + count);
	    } else {
	    	countedName = name + "-" + count;
	    }
	    
		
		return countedName + "." + extension;
	}

	public String getUniqueFileName(String[] files) {
		List<String> fileList = Arrays.asList(files);
		String uniqueFileName = fileName;
		for (int i=1; fileList.contains(uniqueFileName); i++) {
			if (i==MAX_COUNT) {
				return getFileNameByCount(fileList.size());
			}
			uniqueFileName = getFileNameByCount(i);
		}
		return uniqueFileName;
	}
	
}
