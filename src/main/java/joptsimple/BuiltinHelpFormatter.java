/*
 The MIT License

 Copyright (c) 2009 Paul R. Holser, Jr.

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package joptsimple;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import joptsimple.internal.ColumnarData;

import static joptsimple.ParserRules.*;
import static joptsimple.internal.Classes.*;
import static joptsimple.internal.Strings.*;

/**
 * <p>Produces text for a help screen given a set of options.</p>
 *
 * @author <a href="mailto:pholser@alumni.rice.edu">Paul Holser</a>
 */
class BuiltinHelpFormatter implements HelpFormatter {
    private final ColumnarData grid;

    BuiltinHelpFormatter() {
        grid = new ColumnarData( "Option", "Description" );
    }

    public String format( Collection<? extends OptionDescriptor> options ) {
        if ( options.isEmpty() )
            return "No options specified";

        grid.clear();

        Comparator<OptionDescriptor> comparator =
            new Comparator<OptionDescriptor>() {
                public int compare( OptionDescriptor first, OptionDescriptor second ) {
                    return first.options().iterator().next().compareTo( second.options().iterator().next() );
                }
            };

        Set<OptionDescriptor> sorted = new TreeSet<OptionDescriptor>( comparator );
        sorted.addAll( options );

        for ( OptionDescriptor each : sorted )
            addHelpLineFor(each);

        return grid.format();
    }

    private void addHelpLineFor(OptionDescriptor descriptor) {
        if (descriptor.acceptsArguments()) {
            if (descriptor.requiresArgument())
                addHelpLineWithArgument(descriptor, '<', '>');
            else
                addHelpLineWithArgument(descriptor, '[', ']');
        } else {
            addHelpLineFor( descriptor, "" );
        }
    }

    void addHelpLineFor( OptionDescriptor descriptor, String additionalInfo ) {
        grid.addRow(createOptionDisplay(descriptor) + additionalInfo, createDescriptionDisplay(descriptor));
    }

    private void addHelpLineWithArgument(OptionDescriptor descriptor, char begin, char end) {
        String argDescription = descriptor.argumentDescription();
        String typeIndicator = typeIndicator( descriptor );
        StringBuilder collector = new StringBuilder();

        if ( typeIndicator.length() > 0 ) {
            collector.append( typeIndicator );

            if ( argDescription.length() > 0 )
                collector.append( ": " ).append( argDescription );
        }
        else if ( argDescription.length() > 0 )
            collector.append( argDescription );

        String helpLine = collector.length() == 0
            ? ""
            : ' ' + surround( collector.toString(), begin, end );
        addHelpLineFor( descriptor, helpLine );
    }

    private String createOptionDisplay( OptionDescriptor descriptor ) {
        StringBuilder buffer = new StringBuilder();

        for ( Iterator<String> iter = descriptor.options().iterator(); iter.hasNext(); ) {
            String option = iter.next();
            buffer.append( option.length() > 1 ? DOUBLE_HYPHEN : HYPHEN );
            buffer.append( option );

            if ( iter.hasNext() )
                buffer.append( ", " );
        }

        return buffer.toString();
    }

    private String createDescriptionDisplay( OptionDescriptor descriptor ) {
        List<?> defaultValues = descriptor.defaultValues();
        if ( defaultValues.isEmpty() )
            return descriptor.description();

        String defaultValuesDisplay = createDefaultValuesDisplay( defaultValues );
        return descriptor.description() + ' ' + surround( "default: " + defaultValuesDisplay, '(', ')' );
    }

    private String createDefaultValuesDisplay( List<?> defaultValues ) {
        return defaultValues.size() == 1 ? defaultValues.get( 0 ).toString() : defaultValues.toString();
    }

    private static String typeIndicator( OptionDescriptor descriptor ) {
        String indicator = descriptor.argumentTypeIndicator();
        return indicator == null || String.class.getName().equals( indicator )
            ? ""
            : shortNameOf( indicator );
    }
}
