package parser;

import parser.descr.FileDescr;

public interface JavaFileHandler {

    FileDescr getFileDescr();

    String getOriginalContent();

    String buildResult();

    void createImport(String source);

    void removePackageImport(String packageName);

    void removeClassImport(String className);

    void setPackageName(String name);

    void setClassName(String name);

    void setSuperClassName(String className);

    void createClassAnnotation(String source);

    void deleteClassAnnotation(String annotationClassName);

    void createField(String source);

    void renameField(String name, String newName);

    void deleteField(String name);

    void createFieldAnnotation(String fieldName, String source);

    void deleteFieldAnnotation(String fieldName, String annotationClassName);

    void createMethod(String source);

    void deleteMethod(String name, String[] paramTypes);
}
