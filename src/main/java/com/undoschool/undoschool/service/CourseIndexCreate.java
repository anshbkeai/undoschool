package com.undoschool.undoschool.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;

import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch.core.ReindexRequest;
import co.elastic.clients.elasticsearch.core.ReindexResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CourseIndexCreate {

    
        private final ElasticsearchOperations elasticsearchOperations;
         private final ElasticsearchClient elasticsearchClient;

        @Value("${courses-v2}")
        private  String INDEX_NAME_v2 ;
    
        public CourseIndexCreate(ElasticsearchOperations elasticsearchOperations,
                                    ElasticsearchClient elasticsearchClient) {
                // Ensure this matches your application.properties setting   
         
            this.elasticsearchOperations = elasticsearchOperations;
            this.elasticsearchClient = elasticsearchClient;
        }
    
        @PostConstruct
        public void createIndexIfNotExists() {
        IndexOperations indexOps = elasticsearchOperations.indexOps(IndexCoordinates.of(INDEX_NAME_v2));

            if (!indexOps.exists()) {
                indexOps.create();

    // ✅ Apply your exact mapping
            String mappingJson = """
            {
            "properties": {
                "_class": { "type": "keyword", "index": false, "doc_values": false },
                "category": { "type": "keyword" },
                "description": { "type": "text" },
                "gradeRange": { "type": "keyword" },
                "id": { "type": "keyword" },
                "maxAge": { "type": "integer" },
                "minAge": { "type": "integer" },
                "nextSessionDate": { "type": "date", "format": "date_time" },
                "price": { "type": "double" },
                "title": { "type": "text" },
                "type": { "type": "keyword" },
                "suggest": { "type": "completion" }
            }
            }
            """;

            indexOps.putMapping(Document.parse(mappingJson));

            log.info("Created index {} with custom mappings", INDEX_NAME_v2);
        


        
    

            try {
                ReindexRequest reindexRequest = new ReindexRequest.Builder()
                    .source(s -> s.index("courses"))
                    .dest(d -> d.index(INDEX_NAME_v2))
                    .script(
                        new Script.Builder()
                            .source("ctx._source.suggest = [ctx._source.title]") // Remove nextSessionDate field
                            .build()
                    )
                    .build();

                ReindexResponse response = elasticsearchClient.reindex(reindexRequest);
                log.info("Reindexed {} docs from courses to {}", 
                         response.total(), INDEX_NAME_v2);

            } catch (Exception e) {
                log.error("Error during reindexing to {}: {}", INDEX_NAME_v2, e.getMessage(), e);
            }

        } else {
            log.info("Index {} already exists", INDEX_NAME_v2);
        }
    }
    }
    


