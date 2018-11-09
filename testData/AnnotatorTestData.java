/**
 * normal and <info descr="null"><b></info><info descr="null">bold</info><info descr="null"></b></info> text
 * normal and <info descr="null"><i></info><info descr="null">italic</info><info descr="null"></i></info> text
 * normal and <info descr="null"><code></info><info descr="null">code</info><info descr="null"></code></info> text
 * normal and <info descr="null"><tt></info><info descr="null">tt</info><info descr="null"></tt></info> text
 * html link <info descr="null"><a href="http://www.jetbrains.org"></info><info descr="null">JetBrains</info><info descr="null"></a></info>
 * <info descr="null"><a link=""></info>NOT valid link tag<info descr="null"></a></info>
 * <info descr="null"><a name="ooo"></info><info descr="null">not a html link but a_name tag</info><info descr="null"></a></info>
 * {@code<info descr="null"> code_tag</info>}
 * {@code<info descr="null"> <b>not_bold</b></info>}
 * {@code<info descr="null"> multiline</info>
 * <info descr="null">code_tag</info>}
 * {@literal <b>not_bold</b>}
 * {@link #<info descr="null">link_tag</info> link_name}
 * <!-- not a tag --> jjj -->
 *
 * inline <info descr="null"><b></info><info descr="null">bold tag </info><info descr="null"></b></info> and multiline <info descr="null"><b></info><info descr="null"> bold </info>
 * <info descr="null">tag</info><info descr="null"></b></info> with another multiline <info descr="null"><b></info><info descr="null"> bold tag </info>
 * <info descr="null"></b></info> with inline <info descr="null"><b></info><info descr="null">bold tag</info><info descr="null"></b></info>.
 *
 * multiline html tag with nested javadoc tag: <info descr="null"><pre></info><info descr="null"> html tag body </info><info descr="null">{@code</info>
 * <info descr="null"><info descr="null">  html and javadoc tag body</info></info>
 * <info descr="null">}</info><info descr="null"> html tag body </info><info descr="null"></pre></info>
 *
 * multiline tag start <info descr="null"><info descr="null"><b </info></info>
 * <info descr="null"><info descr="null">></info></info><info descr="null"> with multiline end </info><info descr="null"><info descr="null"></b </info></info>
 * <info descr="null"><info descr="null">></info></info>
 *
 * milti line href tag <info descr="null"><info descr="null"><a </info></info>
 * <info descr="null"><info descr="null">href=</info></info>
 * <info descr="null"><info descr="null">"www"></info></info><info descr="null"> with multiline </info>
 * <info descr="null">tag value </info><info descr="null"></a></info>
 */

public class AnnotatorTestData {

}