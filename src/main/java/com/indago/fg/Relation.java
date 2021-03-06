package com.indago.fg;

public enum Relation {
	EQ( "==" ), GE( ">=" ), LE( "<=" );

	private final String symbol;

	private Relation( final String symbol ) {
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return symbol;
	}

	public static Relation forSymbol( final String symbol ) {
		for ( final Relation r : Relation.values() )
			if ( r.symbol.equals( symbol ) ) return r;
		throw new IllegalArgumentException();
	}

	public static boolean holds( final double lhs, final Relation relation, final double rhs ) {
		switch ( relation ) {
		default:
		case EQ:
			return lhs == rhs;
		case GE:
			return lhs >= rhs;
		case LE:
			return lhs <= rhs;
		}
	}
}
