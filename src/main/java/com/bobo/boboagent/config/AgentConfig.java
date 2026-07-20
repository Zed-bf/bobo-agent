package com.bobo.boboagent.config;


import com.alibaba.dashscope.assistants.Assistant;
import com.alibaba.dashscope.embeddings.TextEmbedding;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentParser;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.cohere.CohereScoringModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.scoring.ScoringModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.aggregator.ContentAggregator;
import dev.langchain4j.rag.content.aggregator.ReRankingContentAggregator;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.filter.Filter;
import dev.langchain4j.store.embedding.filter.MetadataFilterBuilder;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;

@Configuration
public class AgentConfig {


    interface Assistant {

        String chat(@UserMessage String userMessage);

        Flux<String> chatByStream(@UserMessage String userMessage);
    }




    @Autowired
    private OpenAiChatModel openAiChatModel;


    @Autowired
    private QwenEmbeddingModel qwenEmbeddingModel;


    // 压缩查询Rag
    @Bean
    public Assistant CompressingQueryAgent() {

        Document document = loadDocument(
                Path.of("F:\\bobo-2026\\bobo-agent\\src\\main\\resources\\rag"), new TextDocumentParser());

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(300, 0))
                .embeddingModel(qwenEmbeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(document);


        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(qwenEmbeddingModel)
                .maxResults(3)
                .build();


        QueryTransformer queryTransformer = new CompressingQueryTransformer(openAiChatModel);

        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .contentRetriever(contentRetriever)
                .queryTransformer(queryTransformer)
                .build();

        return AiServices.builder(Assistant.class)
                .chatLanguageModel(openAiChatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .build();
    }


    @Bean
    public Assistant RoutingQueryAgent(){
        Document document = loadDocument(
                Path.of("F:\\bobo-2026\\bobo-agent\\src\\main\\resources\\rag"), new TextDocumentParser());

        EmbeddingStore<TextSegment> boboEmbeddingStore = embed(Path.of("F:\\bobo-2026\\bobo-agent\\src\\main\\resources\\rag\\biogtaphy-of-bobo.txt"), qwenEmbeddingModel);

        ContentRetriever boboContentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(boboEmbeddingStore)
                .embeddingModel(qwenEmbeddingModel)
                .maxResults(3)
                .minScore(0.5)
                .build();


        EmbeddingStore<TextSegment> qianxuesenEmbeddingStore = embed(Path.of("F:\\bobo-2026\\bobo-agent\\src\\main\\resources\\rag\\biogtaphy-of-qianuxesen.txt"), qwenEmbeddingModel);
        ContentRetriever qianxuesenContentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(qianxuesenEmbeddingStore)
                .embeddingModel(qwenEmbeddingModel)
                .maxResults(3)
                .minScore(0.5)
                .build();

        //创建路由
        Map< ContentRetriever,String> routingMap = new HashMap<>();
        routingMap.put(boboContentRetriever, "波波的传记");
        routingMap.put(qianxuesenContentRetriever, "钱学森的传记");
        QueryRouter queryRouter = new LanguageModelQueryRouter(openAiChatModel, routingMap);
                                    // new DefaultQueryRouter(boboContentRetriever, qianxuesenContentRetriever);

        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .queryRouter(queryRouter)
                .contentRetriever(boboContentRetriever)
                .contentRetriever(qianxuesenContentRetriever)
                .build();


        return AiServices.builder(Assistant.class)
                .retrievalAugmentor(retrievalAugmentor)
                .chatLanguageModel(openAiChatModel)
                .build();
    }



    private static EmbeddingStore<TextSegment> embed(Path documentPath, EmbeddingModel embeddingModel) {
        DocumentParser documentParser = new TextDocumentParser();
        Document document = loadDocument(documentPath, documentParser);

        DocumentSplitter splitter = DocumentSplitters.recursive(300, 0);
        List<TextSegment> segments = splitter.split(document);

        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddings, segments);
        return embeddingStore;
    }


    // 精排
    @Bean
    public Assistant RerankAgent(){

        Document document = loadDocument(Path.of("F:\\bobo-2026\\bobo-agent\\src\\main\\resources\\rag\\flg.txt"), new TextDocumentParser());


        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(300, 0))
                .embeddingModel(qwenEmbeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        ingestor.ingest(document);
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(qwenEmbeddingModel)
                .maxResults(3)
                .build();

        // 精排
        ScoringModel scoringModel = CohereScoringModel.builder()
                .apiKey("sk-xx")
                .modelName("rerank-multilingual-v3.0")
                .build();
        ContentAggregator contentAggregator = ReRankingContentAggregator.builder()
                .scoringModel(scoringModel)
                .minScore(0.8)
                .build();

        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .contentRetriever(contentRetriever)
                .contentAggregator(contentAggregator)
                .build();

        return AiServices.builder(Assistant.class)
                .chatLanguageModel(openAiChatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .build();
    }


    // 元数据
    @Bean
    public Assistant staticMetadataAgent(){

        TextSegment segment = TextSegment.from("路漫漫其修远兮，吾将上下而求索。", Metadata.metadata("author", "屈原"));
        TextSegment segment2 = TextSegment.from("非淡泊无以明志，非宁静无以致远。", Metadata.metadata("author", "诸葛亮"));




        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.add(qwenEmbeddingModel.embed(segment).content(), segment);
        embeddingStore.add(qwenEmbeddingModel.embed(segment2).content(), segment2);


        Filter onlyQuYuan = MetadataFilterBuilder.metadataKey("author").isEqualTo("诸葛亮");

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(qwenEmbeddingModel)
                .filter(onlyQuYuan)
                .maxResults(5)
                .build();

        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .contentRetriever(contentRetriever)
                .build();

        return AiServices.builder(Assistant.class)
                .chatLanguageModel(openAiChatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .build();
    }

    // 动态元数据
    @Bean
    public Assistant dynamicMetadataAgent(){
        TextSegment segment = TextSegment.from("路漫漫其修远兮，吾将上下而求索。", Metadata.metadata("userId", "1"));
        TextSegment segment2 = TextSegment.from("非淡泊无以明志，非宁静无以致远。", Metadata.metadata("userId", "2"));

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.add(qwenEmbeddingModel.embed(segment).content(), segment);
        embeddingStore.add(qwenEmbeddingModel.embed(segment2).content(), segment2);

        Function<Query, Filter> queryToFilter = query -> MetadataFilterBuilder.metadataKey("userId")
                .isEqualTo("2");
                //.isEqualTo(query.metadata().chatMemoryId().toString()

        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(qwenEmbeddingModel)
                .dynamicFilter(queryToFilter)
                .maxResults(5)
                .build();

        RetrievalAugmentor retrievalAugmentor = DefaultRetrievalAugmentor.builder()
                .contentRetriever(contentRetriever)
                .build();

        return AiServices.builder(Assistant.class)
                .chatLanguageModel(openAiChatModel)
                .retrievalAugmentor(retrievalAugmentor)
                .build();


    }


}
