package util;

/**
 * Created with IntelliJ IDEA.
 * User: wmedvede
 * Date: 3/4/14
 * Time: 6:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class ParseTreePrinter {

     /*
    public String printElements(List<FieldDescr> fields, String originalCode) {
        StringBuilder builder = new StringBuilder();
        int offset = 0;

        for (FieldDescr field : fields) {


        }


    }
    */


    /*
    for ( JavaLocalDeclarationDescr local : analysis.getLocalVariablesMap().values() ) {
        locals.add( local );
    }

    StringBuilder initCode = new StringBuilder();
    int lastAdded = 0;
    for ( JavaLocalDeclarationDescr d : locals ) {
        // adding chunk
        initCode.append( originalCode.substring( lastAdded,
                d.getStart() ) );
        lastAdded = d.getEnd();
        // adding variable initializations
        for ( JavaLocalDeclarationDescr.IdentifierDescr id : d.getIdentifiers() ) {
            initCode.append( originalCode.substring( id.getStart(),
                    id.getEnd() ) );
            initCode.append( ";" );
            lastAdded = id.getEnd();
            while ( lastAdded < originalCode.length() && (Character.isWhitespace( originalCode.charAt( lastAdded ) ) || originalCode.charAt( lastAdded ) == ';') ) {
                lastAdded++;
            }
        }
    }
    initCode.append( originalCode.substring( lastAdded ) );

    return initCode.toString();
*/


}
