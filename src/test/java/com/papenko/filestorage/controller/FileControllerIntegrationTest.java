package com.papenko.filestorage.controller;

import com.papenko.filestorage.entity.File;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FileControllerIntegrationTest {
    private static final Pattern ID_JSON = Pattern.compile("\\{\"ID\":\"[-a-zA-Z0-9]+\"}");
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ElasticsearchRestTemplate esTemplate;

    @BeforeEach
    void setUp() {
        esTemplate.indexOps(File.class).delete();
        esTemplate.indexOps(File.class).create();
    }

    @Test
    void post_shouldCreateNewEntity_whenFileIsValid() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(post("/file")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"file1.txt\", \"size\": 0, \"tags\": [\"text\"]}"))
                .andExpect(status().isOk())
                .andReturn();

        assertTrue(ID_JSON.matcher(mvcResult.getResponse().getContentAsString()).matches());
        final SearchHits<File> searchHits = esTemplate.search(Query.findAll(), File.class);
        assertThat(searchHits).isNotEmpty();
        final Iterator<SearchHit<File>> iterator = searchHits.iterator();
        assertTrue(iterator.hasNext());
        final File nextFile = iterator.next().getContent();
        assertFalse(iterator.hasNext());
        assertThat(nextFile).isEqualToIgnoringGivenFields(new File(null, "file1.txt", 0L, List.of("text")), "id");
        assertThat(nextFile.getId()).isNotBlank();
    }

    @Test
    void post_shouldNotCreateNewEntity_whenFileIsInvalid() throws Exception {
        mockMvc.perform(post("/file")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"\", \"size\": 0}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"success\":false,\"error\":\"file name is missing\"}"));

        assertThat(esTemplate.search(Query.findAll(), File.class)).isEmpty();
    }

    @Test
    void delete_shouldReturnNotFoundAndErrorMessage_whenNoDocumentIsFoundBySuchId() throws Exception {
        mockMvc.perform(delete("/file/{ID}", "id0"))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"success\":false,\"error\":\"file not found\"}"));
    }

    @Test
    void delete_shouldReturnOkAndSuccessStatus_whenDocumentIsFoundBySuchId() throws Exception {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId("id0");
        indexQuery.setObject(new File("id0", "name", 0L, null));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));

        mockMvc.perform(delete("/file/{ID}", "id0"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true}"));

        assertThat(esTemplate.search(Query.findAll(), File.class)).isEmpty();
    }

    @Test
    void postTags_shouldReturnNotFoundAndErrorMessage_whenNoDocumentIsFoundBySuchId() throws Exception {
        mockMvc.perform(post("/file/{ID}/tags", "id0")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"tag1\", \"tag2\", \"tag3\"]"))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"success\":false,\"error\":\"file not found\"}"));
    }

    @Test
    void postTags_shouldReturnOkAndSuccessStatus_whenDocumentIsFoundBySuchId() throws Exception {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId("id0");
        indexQuery.setObject(new File("id0", "name", 0L, null));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));

        mockMvc.perform(post("/file/{ID}/tags", "id0")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"tag1\", \"tag2\", \"tag3\"]"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true}"));
    }

    @Test
    void deleteTags_shouldReturnNotFoundAndErrorMessage_whenNoDocumentIsFoundBySuchId() throws Exception {
        mockMvc.perform(delete("/file/{ID}/tags", "id0")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"tag1\", \"tag2\", \"tag3\"]"))
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"success\":false,\"error\":\"file not found\"}"));
    }

    @Test
    void deleteTags_shouldBadRequestAndErrorMessage_whenDocumentIsFoundBySuchIdButDoesNotContainTagsSpecified()
            throws Exception {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId("id0");
        indexQuery.setObject(new File("id0", "name", 0L, null));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));

        mockMvc.perform(delete("/file/{ID}/tags", "id0")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"tag1\", \"tag2\", \"tag3\"]"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"success\":false,\"error\":\"tag not found on file\"}"));
    }

    @Test
    void deleteTags_shouldReturnOkAndSuccessStatus_whenDocumentIsFoundBySuchId() throws Exception {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId("id0");
        indexQuery.setObject(new File("id0", "name", 0L, List.of("tag1", "tag2", "tag3")));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));

        mockMvc.perform(delete("/file/{ID}/tags", "id0")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"tag1\", \"tag2\", \"tag3\"]"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true}"));
    }

    @Test
    void getByTags_shouldReturnOkAndPageWithSingleFile_whenOnlyMatchingDocumentExistsInDb() throws Exception {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId("id0");
        indexQuery.setObject(new File("id0", "name", 0L, List.of("tag1", "tag2", "tag3")));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        esTemplate.indexOps(File.class).refresh();

        mockMvc.perform(get("/file?tags=tag1,tag2,tag3"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"content\":[{\"id\":\"id0\",\"name\":\"name\",\"size\":0,\"tags\":" +
                                "[\"tag1\",\"tag2\",\"tag3\"]}],\"pageable\":{\"sort\":{\"sorted\":false," +
                                "\"unsorted\":true,\"empty\":true},\"pageNumber\":0,\"pageSize\":10,\"offset\":0," +
                                "\"paged\":true,\"unpaged\":false},\"totalPages\":1,\"totalElements\":1," +
                                "\"last\":true,\"number\":0,\"numberOfElements\":1,\"sort\":{\"sorted\":false," +
                                "\"unsorted\":true,\"empty\":true},\"size\":10,\"first\":true,\"empty\":false}"));
    }

    @Test
    void getByTags_shouldReturnOkAndPageWithSingleFile_whenOnlyOneDocumentIsFoundByTagsButThereIsAnotherOneInDb()
            throws Exception {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId("id0");
        indexQuery.setObject(new File("id0", "name", 0L, List.of("tag1", "tag2", "tag3")));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        indexQuery = new IndexQuery();
        indexQuery.setId("id1");
        indexQuery.setObject(new File("id1", "name1", 1L, List.of("tag4", "tag2", "tag3")));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        esTemplate.indexOps(File.class).refresh();

        mockMvc.perform(get("/file?tags=tag1,tag2,tag3"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"content\":[{\"id\":\"id0\",\"name\":\"name\",\"size\":0,\"tags\":" +
                                "[\"tag1\",\"tag2\",\"tag3\"]}],\"pageable\":{\"sort\":{\"sorted\":false," +
                                "\"unsorted\":true,\"empty\":true},\"pageNumber\":0,\"pageSize\":10,\"offset\":0," +
                                "\"paged\":true,\"unpaged\":false},\"totalPages\":1,\"totalElements\":1," +
                                "\"last\":true,\"number\":0,\"numberOfElements\":1,\"sort\":{\"sorted\":false," +
                                "\"unsorted\":true,\"empty\":true},\"size\":10,\"first\":true,\"empty\":false}"));
    }
}
