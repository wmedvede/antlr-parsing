/* changes to this class can break MethodParsing1Test */

package parser;

import java.util.AbstractList;

public class MethodParsing1 {

    private String name;

    private String surname;

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }

    public void setSurname(String surname) { this.surname = surname; }

    public java.util.List<String> getList() { return null; }

    public java.util.AbstractList<String> getNamesList(final int param1, java.lang.Integer param2, java.util.List<java.lang.Integer> param3) { return null; }
}
