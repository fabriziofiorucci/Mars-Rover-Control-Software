package it.ff.Rover.Utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JSON
{
	// Serializes a custom bean into its JSON representation
	public static String Serialize(Object o)
	{
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(Boolean.class, new BooleanSerializer())
				.setPrettyPrinting().create();

		return gson.toJson(o);
	}
}