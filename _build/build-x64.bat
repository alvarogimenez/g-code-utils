rmdir /S /Q "target/dist-x64"
mkdir "target/dist-x64/runtime"
tar xzvf "_build/_jre/jre-8u281-windows-x64.tar.gz" -C "target/dist-x64/runtime"
Launch4jc "_build/_launch4j/config-x64.xml"
tar -a -c -f "target/g-code-utils-x64.zip" -C "target\dist-x64" *