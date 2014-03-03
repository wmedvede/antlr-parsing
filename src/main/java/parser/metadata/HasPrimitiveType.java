package parser.metadata;

/**
 * Created with IntelliJ IDEA.
 * User: wmedvede
 * Date: 3/1/14
 * Time: 12:35 PM
 * To change this template use File | Settings | File Templates.
 */
public interface HasPrimitiveType {

    PrimitiveTypeDesc getPrimitiveType();

    void setPrimitiveType(PrimitiveTypeDesc primitiveType);
}
