package com.nikoyo.sphinx;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBList;

public class Utils {
	public static List<String> convertToString(BasicDBList dbList){
		List<String> result=new ArrayList<String>(dbList.size());
		for(Object o:dbList){
			result.add(o.toString());
		}
		return result;
	}
}
