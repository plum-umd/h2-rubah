
public class UpdateClass_1_2_121_to_1_2_122 implements rubah.ConversionClass {

	// v0.org.h2.command.dml.TransactionCommand to v1.org.h2.command.dml.TransactionCommand
	public void convertStatic(v1.org.h2.command.dml.TransactionCommand ignore) {
		// New fields
		
		// New constants
		v1.org.h2.command.dml.TransactionCommand.SHUTDOWN_COMPACT = 14;
		
		// Fields with type changed
	

		// ---
		v1.org.h2.command.dml.TransactionCommand.BEGIN = 15;
	}

	// ---
	public void convert(v0.org.h2.command.dml.TransactionCommand o0, v1.org.h2.command.dml.TransactionCommand o1) {
		// The value of BEGIN changed from 14 to 15
		if (o0.type == v0.org.h2.command.dml.TransactionCommand.BEGIN) {
			o1.type = v1.org.h2.command.dml.TransactionCommand.BEGIN;
		}
	}

	public void convert(v0.org.h2.bnf.RuleRepeat o0, v1.org.h2.bnf.RuleRepeat o1) {
		// New fields
		o1.comma = false; // Generated HTML of existing instances won't be comma separated
		
		// Fields with type changed
		o1.rule = o0.rule.convert(); // Not a real change, field was made final
		
	}

	// v0.org.h2.bnf.RuleRepeat to v1.org.h2.bnf.RuleRepeat
	public void convertStatic(v1.org.h2.bnf.RuleRepeat ignore) {
		// New fields
		
		// New constants
		v1.org.h2.bnf.RuleRepeat.RAILROAD_DOTS = true;
		
		// Fields with type changed
	}

	// v0.org.h2.tools.Recover to v1.org.h2.tools.Recover
	public void convertStatic(v1.org.h2.tools.Recover ignore) {
		// New fields
		
		// New constants
		v1.org.h2.tools.Recover.SUFFIX_UNCOMMITTED = ".uncommitted.txt";
		
		
		// Fields with type changed
	}

	public void convert(v0.org.h2.store.PageStore o0, v1.org.h2.store.PageStore o1) {
		// New fields
		o1.readCount = 0L;
		// writeCount changed meaning, now is the write count since PageStore was opened
		// writeCountBase + writeCount yields the writeCount since the PageStore was created
		// These values will behave as if the PageStore was opened on update
		o1.writeCountBase = o0.writeCount; 
		o1.writeCount = 0L;
		
		// Fields with type changed
		
	}

	public void convert(v0.org.h2.engine.Database o0, v1.org.h2.engine.Database o1) {
		// New fields
		o1.compactFully = true;
		o1.checkpointAllowed = true;
		o1.checkpointRunning = false;
		
		// Fields with type changed
		
	}

	public void convert(v0.org.h2.index.PageBtreeNode o0, v1.org.h2.index.PageBtreeNode o1) {
		// New fields
		o1.rowCountStored = v1.org.h2.index.PageBtree.UNKNOWN_ROWCOUNT;
		
		// Fields with type changed
		
	}

	public void convert(v0.org.h2.command.ddl.CreateTable o0, v1.org.h2.command.ddl.CreateTable o1) {
		// New fields
		o1.sortedInsertMode = false;
		
		// Fields with type changed
		
	}


	public void convert(v0.org.h2.index.PageIndex o0, v1.org.h2.index.PageIndex o1) {
		// New fields
		o1.sortedInsertMode = false;
		
		// Fields with type changed
		
	}


	public void convert(v0.org.h2.command.dml.Insert o0, v1.org.h2.command.dml.Insert o1) {
		// New fields
		o1.sortedInsertMode = false;
		
		// Fields with type changed
		
	}


	// v0.org.h2.constant.SysProperties to v1.org.h2.constant.SysProperties
	public void convertStatic(v1.org.h2.constant.SysProperties ignore) {
		// New fields
		
		// New constants
		v1.org.h2.constant.SysProperties.MAX_COMPACT_TIME = 1000; 
		v1.org.h2.constant.SysProperties.MAX_COMPACT_COUNT = Integer.MAX_VALUE;

		// Fields with type changed
	}
	
	public void convertStatic(v1.org.h2.util.Resources ignore) {
		v1.org.h2.util.Resources.loadFromZip();
	}

	// ###########
	// Still to do
	// ###########
	
	public void convert(v0.org.h2.tools.Console o0, v1.org.h2.tools.Console o1) {
		// New fields
		o1.icon22 = null; // v1.org.h2.tools.Console.loadImage("/org/h2/res/h2-22.png");
		
		
		// Fields with type changed
		
	}

}
