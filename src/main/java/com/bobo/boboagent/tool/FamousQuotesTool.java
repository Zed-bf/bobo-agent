package com.bobo.boboagent.tool;

import com.bobo.boboagent.utils.FamousQuotesUtil;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

public class FamousQuotesTool {





    @Tool("返回给定人名的名人名言")
    public String getFamousQuotes(@P("应返回名人名言的作者名") String name){
        return  FamousQuotesUtil.getFamousQuotes(name);

    }
}
