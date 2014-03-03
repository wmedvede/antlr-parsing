package parser.metadata;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: wmedvede
 * Date: 3/1/14
 * Time: 11:31 AM
 * To change this template use File | Settings | File Templates.
 */
public interface HasTypeArguments {

    List<TypeArgumentDesc> getArguments();

    void addArgument(TypeArgumentDesc typeArgument);

}
