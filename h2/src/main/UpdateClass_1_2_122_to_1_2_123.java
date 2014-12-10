import rubah.Rubah;
import rubah.RubahThread;


public class UpdateClass_1_2_122_to_1_2_123 implements rubah.ConversionClass {

	public void convert(v0.org.h2.engine.Session o0, v1.org.h2.engine.Session o1) {
		// New fields
		o1.scopeIdentity = v1.org.h2.value.ValueLong.get(0);
		
		// Fields with type changed
	}

	// v0.org.h2.fulltext.FullText to v1.org.h2.fulltext.FullText
	public void convertStatic(v1.org.h2.fulltext.FullText ignore) {
		// New fields
		
		// New constants
		v1.org.h2.fulltext.FullText.FIELD_SCORE = "SCORE";
		
		// Fields with type changed
	}


	// v0.org.h2.expression.Function to v1.org.h2.expression.Function
	public void convertStatic(v1.org.h2.expression.Function ignore) {
		// New fields
		
		// New constants
		v1.org.h2.expression.Function.SCOPE_IDENTITY = 154;
		v1.org.h2.expression.Function.addFunctionNotDeterministic("SCOPE_IDENTITY", v1.org.h2.expression.Function.SCOPE_IDENTITY, 0, v1.org.h2.value.Value.LONG);
		
		// Re-add functions with changed number
		v1.org.h2.expression.Function.addFunctionNotDeterministic("AUTOCOMMIT", v1.org.h2.expression.Function.AUTOCOMMIT, 0, v1.org.h2.value.Value.BOOLEAN);
		v1.org.h2.expression.Function.addFunctionNotDeterministic("READONLY", v1.org.h2.expression.Function.READONLY, 0, v1.org.h2.value.Value.BOOLEAN);
		v1.org.h2.expression.Function.addFunction("DATABASE_PATH", v1.org.h2.expression.Function.DATABASE_PATH, 0, v1.org.h2.value.Value.STRING);
		v1.org.h2.expression.Function.addFunction("LOCK_TIMEOUT", v1.org.h2.expression.Function.LOCK_TIMEOUT, 0, v1.org.h2.value.Value.INT);
		
		// Fields with type changed
	}

	public void convert(v0.org.h2.server.web.WebThread o0, v1.org.h2.server.web.WebThread o1) {
		// New fields
		RubahThread t = new RubahThread(o1);
		o1.thread = t;
		Rubah.redirectThreadAfterUpdate(o0, t);
		
		// Fields moved to superclass
		o1.server = o0.server.convert(o1.server);
		o1.attributes = o0.attributes;
		o1.listenerLastState = o0.listenerLastState;
		o1.listenerLastEvent = o0.listenerLastEvent;
		o1.cache = o0.cache;
		o1.stop = o0.stop;
		o1.headerLanguage = o0.headerLanguage;
		
		// Fields with type changed
	}

}
