package it.ff.Rover.Utils;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class BooleanSerializer implements JsonSerializer<Boolean>
{
	@Override
	public JsonElement serialize(Boolean aBoolean, Type type,
			JsonSerializationContext jsonSerializationContext)
	{
		if (aBoolean)
		{
			return new JsonPrimitive(1);
		}
		return new JsonPrimitive(0);
	}
}