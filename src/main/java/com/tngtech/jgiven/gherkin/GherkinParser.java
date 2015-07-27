package com.tngtech.jgiven.gherkin;

import java.io.*;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.CharSource;
import com.google.common.io.CharStreams;
import com.google.common.io.Files;

import gherkin.Parser;
import gherkin.ast.Feature;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Step;

public class GherkinParser {
    private static final String INDENT = "    ";
    private static final String INDENT_2 = Strings.repeat( INDENT, 2 );

    public static void main( String... args ) throws IOException {
        Parser<Feature> parser = new Parser<Feature>();
        CharSource source = Files.asCharSource( new File( args[0] ), Charsets.UTF_8 );
        BufferedReader bufferedReader = source.openBufferedStream();
        try {
            Feature feature = parser.parse( bufferedReader );

            PrintWriter writer = new PrintWriter( new BufferedWriter( CharStreams.asWriter( System.out ) ) );
            generateJGivenCode( feature, writer );
            writer.flush();
        } finally {
            bufferedReader.close();
        }
    }

    private static void generateJGivenCode( Feature feature, PrintWriter writer ) throws IOException {
        writer.println( "public class " + toCamelCase( feature.getDescription() ) + "Test extends " );
        writer.println( INDENT_2 + "ScenarioTest<GivenState, WhenAction, ThenOutcome> { " );

        for( ScenarioDefinition scenarioDefinition : feature.getScenarioDefinitions() ) {
            generateScenario( scenarioDefinition, writer );
        }

        writer.println( "}" );
    }

    private static void generateScenario( ScenarioDefinition scenarioDefinition, PrintWriter writer ) {
        writer.println();
        writer.println( INDENT + "@Test" );
        writer.println( INDENT + "public void " + toSnakeCase( scenarioDefinition.getName() ) + " {" );

        boolean first = true;
        for( Step step : scenarioDefinition.getSteps() ) {
            generateStep( step, first, writer );
            first = false;
        }
        writer.println( ";" );
        writer.println( INDENT + "}" );

    }

    private static void generateStep( Step step, boolean first, PrintWriter writer ) {
        String keyword = step.getKeyword().toLowerCase().trim();
        if( !first ) {
            if( keyword.equals( "and" ) ) {
                writer.println( "." );
                writer.print( INDENT );
            } else {
                writer.println( ";" );
                writer.println();
            }
        }

        writer.print( INDENT_2 + keyword + "()." );
        writer.print( toSnakeCase( step.getText() ) + "()" );

    }

    private static String toSnakeCase( String name ) {
        return name.replace( ' ', '_' );
    }

    private static String toCamelCase( String name ) {
        return name.replace( " ", "" ).replaceAll( "[^A-Za-z]", "_" );
    }

}
