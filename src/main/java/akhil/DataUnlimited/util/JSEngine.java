package akhil.DataUnlimited.util;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;

import com.oracle.truffle.js.scriptengine.GraalJSScriptEngine;

public class JSEngine {
	
	
	private static ScriptEngine getJSEngine()
	{
		Engine graalEngine = Engine.newBuilder()
	            .allowExperimentalOptions(true)
	            .option("engine.WarnInterpreterOnly", "false")
	            .build();
		
		ScriptEngine se = GraalJSScriptEngine.create(graalEngine, Context.newBuilder("js"));
		
		return se;
	}
	
	public static Object eval(String s) throws ScriptException
	{
		return getJSEngine().eval(s);
	}
	

}
