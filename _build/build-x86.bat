rmdir /S /Q "target/dist-x86"
mkdir "target/dist-x86/runtime"
tar xzvf "_build/_jre/jre-8u281-windows-i586.tar.gz" -C "target/dist-x86/runtime"
Launch4jc "_build/_launch4j/config-x86.xml"
tar -a -c -f "target/g-code-utils-x86.zip" -C "target\dist-x86" *