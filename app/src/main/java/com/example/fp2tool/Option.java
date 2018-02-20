package com.example.fp2tool;

public class Option implements Comparable<Option> {
	
	private String name;
	private String data;
	private String path;
	
	public Option (String n, String d, String p) {
		this.name = n;
		this.data = d;
		this.path = p;
	}
	public String getName() {	return this.name;	}
	public String getData()	{	return this.data;	}
	public String getPath() {	return this.path;	}
	
	@Override
	public int compareTo(Option o) {
		if (this.name != null)
			return this.name.toLowerCase().compareTo(o.getName().toLowerCase()); 
		else 
			throw new IllegalArgumentException();
	}
}

