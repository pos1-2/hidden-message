/* 
 * Copyright 2015 Florian Hassanen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pos1_2.codegen;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class Main {
	private static abstract class Variable {
		private final String name;
		protected final char character;

		public Variable(String name, char character) {
			this.name = name;
			this.character = character;
		}

		public String getName() {
			return name;
		}

		public abstract String getLiteral();

		public abstract String getType();

		public abstract String getPrintType();

		private String getCapitalizedName() {
			return Character.toString(Character.toUpperCase(name.charAt(0))) + name.substring(1);
		}

		public String getGetterName() {
			return "get" + getCapitalizedName();
		}

		public String getSetterName() {
			return "set" + getCapitalizedName();
		}

		private static Set<String> used = new HashSet<>();

		public static Variable randomVariable(char value) {
			Random r = new Random();

			int len = r.nextInt(5) + 3;

			StringBuffer b = new StringBuffer();

			for (int i = 0; i < len; ++i) {
				b.append((char) ('a' + r.nextInt(26)));
			}

			String name = b.toString();

			if (used.contains(name)) {
				return randomVariable(value);
			}

			used.add(name);

			switch (r.nextInt(3)) {
			case 0:
				return new StringVariable(name, value);
			case 1:
				return new CharacterVariable(name, value);
			default:
				return new IntVariable(name, value);
			}
		}
	}

	private static class StringVariable extends Variable {
		public StringVariable(String name, char character) {
			super(name, character);
		}

		@Override
		public String getLiteral() {
			return "\"" + charLiteral(character) + "\"";
		}

		@Override
		public String getType() {
			return "String";
		}

		@Override
		public String getPrintType() {
			return null;
		}
	}

	private static class CharacterVariable extends Variable {
		public CharacterVariable(String name, char character) {
			super(name, character);
		}

		@Override
		public String getLiteral() {
			return "'" + charLiteral(character) + "'";
		}

		@Override
		public String getType() {
			return "char";
		}

		@Override
		public String getPrintType() {
			return null;
		}
	}

	private static class IntVariable extends Variable {
		public IntVariable(String name, char character) {
			super(name, character);
		}

		@Override
		public String getLiteral() {
			return Integer.toString(character);
		}

		@Override
		public String getType() {
			return "int";
		}

		@Override
		public String getPrintType() {
			return "char";
		}
	}

	private static CharsetEncoder e = Charset.forName("US-ASCII").newEncoder();

	private static String charLiteral(char ch) {
		return (e.canEncode(ch) ? (m.containsKey(ch) ? m.get(ch) : Objects.toString(ch))
				: ("\\u" + String.format("%4s", Integer.toString(ch, 16)).replaceAll(" ", "0")));
	}

	private static Map<Character, String> m = new HashMap<Character, String>() {
		private static final long serialVersionUID = 8814287921257227938L;

		{
			put('\t', "\\t");
			put('\b', "\\b");
			put('\n', "\\n");
			put('\r', "\\r");
			put('\f', "\\f");
			put('\'', "\\\'");
			put('\"', "\\\"");
			put('\\', "\\\\");
		}
	};

	private static List<Variable> variables = new ArrayList<>();

	public static void main(String[] args) throws IOException {

		try (InputStreamReader isr = new InputStreamReader(new FileInputStream("input.txt"))) {
			int c;

			System.out.println("public class CodeGen {");

			System.out.println("public CodeGen() {");
			System.out.println("set();");
			System.out.println("print();");
			System.out.println("}");

			System.out.println("private void print() {");

			while ((c = isr.read()) != -1) {
				Variable v = Variable.randomVariable((char) c);
				variables.add(v);

				System.out.println("System.out.print(" + (v.getPrintType() == null ? "" : "(" + v.getPrintType() + ")")
						+ v.getGetterName() + "());");
			}
		}

		System.out.println("}");

		System.out.println("private void set() {");

		Collections.shuffle(variables);

		for (Variable v : variables) {
			System.out.println(v.getSetterName() + "(" + v.getLiteral() + ");");
		}

		System.out.println("}");

		Collections.shuffle(variables);

		for (Variable v : variables) {
			System.out.println("private " + v.getType() + " " + v.getName() + ";");
		}

		System.out.println("}");
	}
}
