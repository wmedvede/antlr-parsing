package parser.descr;

import java.util.List;


public interface HasTypeArguments {

    List<TypeArgumentDescr> getArguments();

    void addArgument(TypeArgumentDescr typeArgument);

}