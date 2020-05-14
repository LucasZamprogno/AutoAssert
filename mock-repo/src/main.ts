export class Main {

	private static readonly someSymbol: symbol = Symbol("descriptor");

	public static returnBoolean(): boolean {
		return true;
	}

	public static returnNumber(): number {
		return 1;
	}

	public static returnString(): string {
		return "foo"
	}
	
	public static returnSymbol(): symbol {
		return this.someSymbol;
	}
	
	public static returnFunction(): () => void {
		return function() {
			console.log("foo");
		}
	}
	
	public static returnArray(): any[] {
		return ["foo", null, 1];
	}
	
	public static returnNull(): null {
		return null;
	}
	
	public static returnVoid(): void {
		console.log("side effect");
	}
	
	public static returnSet(): Set<any[]> {
		return new Set(Main.returnArray());
	}
	
	public static returnObject(): Object {
		return {
			"foo": "bar",
			"num": 1,
			"arr": Main.returnArray(),
			"sub": {"a" : 1}
		}
	}
	
	public static returnClass(): SomeOtherClass {
		return new SomeOtherClass(1, "foo");
	}
}

export class SomeOtherClass {
	private field1: number;
	private field2: string;

	constructor(first: number, second: string) {
		this.field1 = first;
		this.field2 = second;
	}

	public method1(): number {
		return this.field1;
	}

	public method2(): string {
		return this.field2;
	}
}