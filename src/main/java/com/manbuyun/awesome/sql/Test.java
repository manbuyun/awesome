package com.manbuyun.awesome.sql;

/**
 * User: jinhai
 * Date: 2018-11-12
 */
public class Test {

    /**
     * 需要引入 antlr4 SqlBase.g4 生成的所有java文件，同时加上package
     *
     * @param args
     * @throws Exception
     */
    //public static void main(String[] args) {
    //    // 新建词法分析器，处理输入InputStream
    //    com.manbuyun.awesome.sql.com.manbuyun.awesome.sql.SqlBaseLexer lexer = new com.manbuyun.awesome.sql.com.manbuyun.awesome.sql.SqlBaseLexer(CharStreams.fromString("{1,{2,3},4}"));
    //
    //    // 新建词法分析器缓冲区，存储词法分析器生成的词法符号
    //    CommonTokenStream tokens = new CommonTokenStream(lexer);
    //
    //    // 新建语法分析器，处理词法符号缓冲区的内容
    //    com.manbuyun.awesome.sql.com.manbuyun.awesome.sql.SqlBaseParser parser = new com.manbuyun.awesome.sql.com.manbuyun.awesome.sql.SqlBaseParser(tokens);
    //
    //    // 从init规则(起始规则名)，开始语法分析
    //    ParseTree tree = parser.init();
    //
    //    // 用LISP风格打印生成的树
    //    System.out.println(tree.toStringTree(parser));
    //
    //    // 新建语法分析树遍历器
    //    ParseTreeWalker walker = new ParseTreeWalker();
    //
    //    // 遍历语法分析树，触发回调监听器
    //    walker.walk(new StringListener(), tree);
    //
    //    System.out.println("END");
    //}
    //
    //public static class StringListener extends com.manbuyun.awesome.sql.SqlBaseBaseListener {
    //    @Override
    //    public void enterValue(com.manbuyun.awesome.sql.com.manbuyun.awesome.sql.SqlBaseParser.ValueContext ctx) {
    //        Integer value = Integer.valueOf(ctx.INT().getText());
    //        System.out.println(value);
    //    }
    //}
}
