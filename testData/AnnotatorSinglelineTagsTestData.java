/**
 * normal and <info descr="MY_BORDERED"><b></info><info descr="MY_BOLD">bold</info><info descr="MY_BORDERED"></b></info> text
 * normal and <info descr="MY_BORDERED"><i></info><info descr="MY_ITALIC">italic</info><info descr="MY_BORDERED"></i></info> text
 * normal and <info descr="MY_BORDERED"><code></info><info descr="code_tag">code</info><info descr="MY_BORDERED"></code></info> text
 * normal and <info descr="MY_BORDERED"><tt></info><info descr="code_tag">tt</info><info descr="MY_BORDERED"></tt></info> text
 * html link <info descr="MY_BORDERED"><a href="http://www.jetbrains.org"></info><info descr="html_link_tag">JetBrains</info><info descr="MY_BORDERED"></a></info>
 * <info descr="MY_BORDERED"><a link=""></info>NOT valid link tag<info descr="MY_BORDERED"></a></info>
 * <info descr="MY_BORDERED"><a name="ooo"></info><info descr="MY_BOLD">not a html link but a_name tag</info><info descr="MY_BORDERED"></a></info>
 * <info descr="MY_BORDERED">{@code</info><info descr="code_tag"> code_tag</info><info descr="MY_BORDERED">}</info>
 * <info descr="MY_BORDERED">{@code</info><info descr="code_tag"> <b>not_bold</b></info><info descr="MY_BORDERED">}</info>
 * <info descr="MY_BORDERED">{@literal</info> <b>not_bold</b><info descr="MY_BORDERED">}</info>
 * <info descr="MY_BORDERED">{@link</info> #<info descr="link_tag">link_tag</info> link_name<info descr="MY_BORDERED">}</info>
 * <!-- not a tag --> jjj -->
 * escaped chars: <info descr="MY_BORDERED">&amp;</info><info descr="MY_BORDERED">&amp;</info>
 * &not_escaped_char;
 * <info descr="MY_BORDERED">{@code</info><info descr="MY_BORDERED">}</info>
 * <info descr="MY_BORDERED">{@code</info><info descr="code_tag"> incomplete_code_tag</info>
 */

public class AnnotatorSinglelineTagsTestData {

}