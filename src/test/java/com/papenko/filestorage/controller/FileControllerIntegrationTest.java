package com.papenko.filestorage.controller;

import com.papenko.filestorage.entity.File;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.papenko.filestorage.constant.EnvironmentVariable.SPRING_PROFILES_ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FileControllerIntegrationTest {
    private static final Pattern ID_JSON = Pattern.compile("\\{\"ID\":\"[-_a-zA-Z0-9]+\"}");
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ElasticsearchRestTemplate esTemplate;

    @BeforeAll
    static void beforeAll() throws ReflectiveOperationException {
        updateProfile("test");
    }

    @AfterAll
    static void afterAll() throws ReflectiveOperationException {
        updateProfile("local");
    }


    private static void updateProfile(String val) throws ReflectiveOperationException {
        Map<String, String> env = System.getenv();
        Field field = env.getClass().getDeclaredField("m");
        field.setAccessible(true);
        ((Map<String, String>) field.get(env)).put(SPRING_PROFILES_ACTIVE, val);
    }

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
        final Iterator<SearchHit<File>> iterator = esTemplate.search(Query.findAll(), File.class).iterator();
        assertTrue(iterator.hasNext());
        final File nextFile = iterator.next().getContent();
        assertFalse(iterator.hasNext());
        final File other = new File(null, "file1.txt", 0L, Set.of("text", "document"));
        assertThat(nextFile).isEqualToIgnoringGivenFields(other, "id");
        assertThat(nextFile.getId()).isNotBlank();
    }

    @Test
    void post_shouldNotUpdateEntity_whenFileExistsInDbByIdProvided() throws Exception {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId("id0");
        indexQuery.setObject(new File("id0", "name", 0L, null));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        esTemplate.indexOps(File.class).refresh();

        final MvcResult mvcResult = mockMvc.perform(post("/file")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"ID\":\"id0\",\"name\": \"file1.ddd\", \"size\": 0, \"tags\": [\"text\"]}"))
                .andExpect(status().isOk())
                .andReturn();

        assertTrue(ID_JSON.matcher(mvcResult.getResponse().getContentAsString()).matches());
        final Iterator<SearchHit<File>> iterator = esTemplate.search(Query.findAll(), File.class).iterator();
        assertTrue(iterator.hasNext());
        iterator.next();
        assertTrue(iterator.hasNext());
        iterator.next();
        assertFalse(iterator.hasNext());
    }

    @Test
    void post_shouldAddArchiveTag_whenFileIsOfRarExtension() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(post("/file")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"file1.rar\", \"size\": 0, \"tags\": [\"text\"]}"))
                .andExpect(status().isOk())
                .andReturn();

        assertTrue(ID_JSON.matcher(mvcResult.getResponse().getContentAsString()).matches());
        final Iterator<SearchHit<File>> iterator = esTemplate.search(Query.findAll(), File.class).iterator();
        assertTrue(iterator.hasNext());
        final File nextFile = iterator.next().getContent();
        assertFalse(iterator.hasNext());
        final File other = new File(null, "file1.rar", 0L, Set.of("text", "archive"));
        assertThat(nextFile).isEqualToIgnoringGivenFields(other, "id");
        assertThat(nextFile.getId()).isNotBlank();
    }

    @Test
    void post_shouldAddArchiveTagAndBeAbleToDeleteIt_whenFileIsOfZipExtension() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(post("/file")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"file1.zip\", \"size\": 0, \"tags\": [\"text\"]}"))
                .andExpect(status().isOk())
                .andReturn();

        assertTrue(ID_JSON.matcher(mvcResult.getResponse().getContentAsString()).matches());
        Iterator<SearchHit<File>> iterator = esTemplate.search(Query.findAll(), File.class).iterator();
        assertTrue(iterator.hasNext());
        File nextFile = iterator.next().getContent();
        assertFalse(iterator.hasNext());
        final File other = new File(null, "file1.zip", 0L, Set.of("text", "archive"));
        assertThat(nextFile).isEqualToIgnoringGivenFields(other, "id");
        assertThat(nextFile.getId()).isNotBlank();

        mockMvc.perform(delete("/file/{ID}/tags", nextFile.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"text\", \"archive\"]"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true}"));

        iterator = esTemplate.search(Query.findAll(), File.class).iterator();
        assertTrue(iterator.hasNext());
        nextFile = iterator.next().getContent();
        assertFalse(iterator.hasNext());
        assertThat(nextFile).isEqualToIgnoringGivenFields(new File(null, "file1.zip", 0L, Set.of()), "id");
        assertThat(nextFile.getId()).isNotBlank();
    }

    @Test
    void post_shouldNotCreateNewEntity_whenFileNameIsMissing() throws Exception {
        mockMvc.perform(post("/file")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"\", \"size\": 0}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"success\":false,\"error\":\"file name is missing\"}"));

        assertThat(esTemplate.search(Query.findAll(), File.class)).isEmpty();
    }

    @Test
    void post_shouldNotCreateNewEntity_whenFileSizeIsMissing() throws Exception {
        mockMvc.perform(post("/file")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"name0\", \"size\": null}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"success\":false,\"error\":\"file size is missing\"}"));

        assertThat(esTemplate.search(Query.findAll(), File.class)).isEmpty();
    }

    @Test
    void post_shouldNotCreateNewEntity_whenFileSizeIsNegative() throws Exception {
        mockMvc.perform(post("/file")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"name0\", \"size\": -1}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"success\":false,\"error\":\"file size is negative\"}"));

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
        esTemplate.indexOps(File.class).refresh();

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
    void postTags_shouldReturnOkAndSuccessStatus_whenDocumentIsFoundBySuchIdAndItHadNoTags() throws Exception {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId("id0");
        indexQuery.setObject(new File("id0", "name", 0L, null));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        esTemplate.indexOps(File.class).refresh();

        mockMvc.perform(post("/file/{ID}/tags", "id0")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"tag1\", \"tag2\", \"tag3\"]"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true}"));

        final SearchHits<File> searchHits = esTemplate.search(Query.findAll(), File.class);
        final Iterator<SearchHit<File>> iterator = searchHits.iterator();
        assertTrue(iterator.hasNext());
        final File nextFile = iterator.next().getContent();
        assertFalse(iterator.hasNext());
        var tags = Set.of("tag1", "tag2", "tag3");
        assertThat(nextFile).isEqualToIgnoringGivenFields(new File(null, "name", 0L, tags), "id");
        assertThat(nextFile.getId()).isNotBlank();
    }

    @Test
    void postTags_shouldReturnOkAndSuccessStatus_whenDocumentIsFoundBySuchIdAndItHadSomeUniqueTags() throws Exception {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId("id0");
        indexQuery.setObject(new File("id0", "name", 0L, Set.of("yo", "yolo")));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        esTemplate.indexOps(File.class).refresh();

        mockMvc.perform(post("/file/{ID}/tags", "id0")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"tag1\", \"tag2\", \"tag3\"]"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true}"));

        final SearchHits<File> searchHits = esTemplate.search(Query.findAll(), File.class);
        final Iterator<SearchHit<File>> iterator = searchHits.iterator();
        assertTrue(iterator.hasNext());
        final File nextFile = iterator.next().getContent();
        assertFalse(iterator.hasNext());
        var tags = Set.of("yo", "yolo", "tag1", "tag2", "tag3");
        assertThat(nextFile).isEqualToIgnoringGivenFields(new File(null, "name", 0L, tags), "id");
        assertThat(nextFile.getId()).isNotBlank();
    }

    @Test
    void postTags_shouldReturnOkAndSuccessStatus_whenDocumentIsFoundBySuchIdAndItHadSomeRepeatingTags()
            throws Exception {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId("id0");
        indexQuery.setObject(new File("id0", "name", 0L, Set.of("yo", "yolo")));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        esTemplate.indexOps(File.class).refresh();

        mockMvc.perform(post("/file/{ID}/tags", "id0")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"yo\", \"hello\", \"hi\"]"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true}"));

        final SearchHits<File> searchHits = esTemplate.search(Query.findAll(), File.class);
        final Iterator<SearchHit<File>> iterator = searchHits.iterator();
        assertTrue(iterator.hasNext());
        final File nextFile = iterator.next().getContent();
        assertFalse(iterator.hasNext());
        var tags = Set.of("yo", "yolo", "hello", "hi");
        assertThat(nextFile).isEqualToIgnoringGivenFields(new File(null, "name", 0L, tags), "id");
        assertThat(nextFile.getId()).isNotBlank();
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
    void deleteTags_shouldReturnBadRequestAndErrorMessage_whenDocumentIsFoundBySuchIdButDoesNotContainTagsSpecified()
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
        indexQuery.setObject(new File("id0", "name", 0L, Set.of("tag1", "tag2", "tag3")));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        esTemplate.indexOps(File.class).refresh();

        mockMvc.perform(delete("/file/{ID}/tags", "id0")
                .contentType(MediaType.APPLICATION_JSON)
                .content("[\"tag1\", \"tag2\", \"tag3\"]"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"success\":true}"));

        final SearchHits<File> searchHits = esTemplate.search(Query.findAll(), File.class);
        final Iterator<SearchHit<File>> iterator = searchHits.iterator();
        assertTrue(iterator.hasNext());
        final File nextFile = iterator.next().getContent();
        assertFalse(iterator.hasNext());
        assertThat(nextFile).isEqualToIgnoringGivenFields(new File(null, "name", 0L, Set.of()), "id");
        assertThat(nextFile.getId()).isNotBlank();
    }

    @Test
    void getByTagsAndName_shouldReturnOkAndPageWithSingleFile_whenOnlyOneMatchingByTagsDocumentExistsInDb()
            throws Exception {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId("id0");
        indexQuery.setObject(new File("id0", "name", 0L, Set.of("tag1", "tag2", "tag3")));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        esTemplate.indexOps(File.class).refresh();

        mockMvc.perform(get("/file?tags=tag1,tag2,tag3"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"total\":1,\"page\":[{\"id\":\"id0\",\"name\":\"name\",\"size\":0,\"tags\":" +
                                "[\"tag1\",\"tag2\",\"tag3\"]}]}"));
    }

    @Test
    void getByTagsAndName_shouldReturnOkAndPageWithSingleFile_whenOnlyOneMatchingByTagsWithSpacesDocumentExistsInDb()
            throws Exception {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId("id0");
        indexQuery.setObject(new File("id0", "name", 0L, Set.of("tag 1", "tag 2", "tag 3")));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        esTemplate.indexOps(File.class).refresh();

        mockMvc.perform(get("/file?tags=tag%201,tag%202,tag%203"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"total\":1,\"page\":[{\"id\":\"id0\",\"name\":\"name\",\"size\":0,\"tags\":" +
                                "[\"tag 1\",\"tag 2\",\"tag 3\"]}]}"));
    }

    @Test
    void getByTagsAndName_shouldReturnOkAndPageWithSingleFile_whenOnlyOneDocumentIsFoundByTagsButThereIsAnotherOneInDb()
            throws Exception {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId("id0");
        indexQuery.setObject(new File("id0", "name", 0L, Set.of("tag1", "tag2", "tag3")));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        indexQuery = new IndexQuery();
        indexQuery.setId("id1");
        indexQuery.setObject(new File("id1", "name1", 1L, Set.of("tag4", "tag2", "tag3")));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        esTemplate.indexOps(File.class).refresh();

        mockMvc.perform(get("/file?tags=tag1,tag2,tag3"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"total\":1,\"page\":[{\"id\":\"id0\",\"name\":\"name\",\"size\":0,\"tags\":" +
                                "[\"tag1\",\"tag2\",\"tag3\"]}]}"));
    }

    @Test
    void getByTagsAndName_shouldReturnOkAndPageWithSingleFile_whenOnlyTwoMatchingDocumentsExistInDbButWeRequestOne()
            throws Exception {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId("id0");
        indexQuery.setObject(new File("id0", "name", 0L, Set.of("tag1", "tag2", "tag3")));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        indexQuery = new IndexQuery();
        indexQuery.setId("id1");
        indexQuery.setObject(new File("id1", "name1", 1L, Set.of("tag1", "tag2", "tag3")));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        esTemplate.indexOps(File.class).refresh();

        mockMvc.perform(get("/file?tags=tag1,tag2,tag3&size=1"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"total\":2,\"page\":[{\"id\":\"id0\",\"name\":\"name\",\"size\":0,\"tags\":" +
                                "[\"tag1\",\"tag2\",\"tag3\"]}]}"));
    }

    @Test
    void getByTagsAndName_shouldReturnOkAndPageWithSingleFile_whenOnlyOneMatchingByNameDocumentExistsInDb()
            throws Exception {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId("id0");
        indexQuery.setObject(new File("id0", "yolo.name0.txt", 0L, null));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        esTemplate.indexOps(File.class).refresh();

        mockMvc.perform(get("/file?q=name"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"total\":1,\"page\":[{\"id\":\"id0\",\"name\":\"yolo.name0.txt\",\"size\":0," +
                                "\"tags\":[]}]}"));
    }

    @Test
    void getByTagsAndName_shouldReturnOkAndPageWithSingleFile_whenOnlyOneMatchingByNameWithWhitespacesDocumentExistsInDb()
            throws Exception {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId("id0");
        indexQuery.setObject(new File("id0", "yolo name0.txt", 0L, null));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        esTemplate.indexOps(File.class).refresh();

        mockMvc.perform(get("/file?q=yolo%20name"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"total\":1,\"page\":[{\"id\":\"id0\",\"name\":\"yolo name0.txt\",\"size\":0," +
                                "\"tags\":[]}]}"));
    }

    @Test
    void getByTagsAndName_shouldReturnOkAndPageWithSingleFile_whenOnlyOneDocumentIsFoundByNameButThereIsAnotherOneInDb()
            throws Exception {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId("id0");
        indexQuery.setObject(new File("id0", "name.txt", 0L, null));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        indexQuery = new IndexQuery();
        indexQuery.setId("id1");
        indexQuery.setObject(new File("id1", "name1.vid", 1L, null));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        esTemplate.indexOps(File.class).refresh();

        mockMvc.perform(get("/file?q=name1"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"total\":1,\"page\":[{\"id\":\"id1\",\"name\":\"name1.vid\",\"size\":1," +
                                "\"tags\":[]}]}"));
    }

    @Test
    void getByTagsAndName_shouldReturnOkAndPageWithTwoFiles_whenOnlyTwoMatchingByNameAndTagsDocumentExistInDb()
            throws Exception {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId("id0");
        indexQuery.setObject(new File("id0", "yolo.name0.txt", 0L, Set.of("yo")));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        indexQuery = new IndexQuery();
        indexQuery.setId("id1");
        indexQuery.setObject(new File("id1", "yolo.name1.doc", 1L, Set.of("yo")));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        esTemplate.indexOps(File.class).refresh();

        mockMvc.perform(get("/file?q=name&tags=yo"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"total\":2,\"page\":[{\"id\":\"id0\",\"name\":\"yolo.name0.txt\",\"size\":0," +
                                "\"tags\":[\"yo\"]},{\"id\":\"id1\",\"name\":\"yolo.name1.doc\"," +
                                "\"size\":1,\"tags\":[\"yo\"]}]}"));
    }

    @Test
    void getByTagsAndName_shouldReturnOkAndPageWithTwoFiles_whenOnlyTwoDocumentIsFoundByNameAndTagsButThereIsMoreInDb()
            throws Exception {
        IndexQuery indexQuery = new IndexQuery();
        indexQuery.setId("id-0");
        indexQuery.setObject(new File("id-0", "name.mp3", 0L, null));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        indexQuery = new IndexQuery();
        indexQuery.setId("id-1");
        indexQuery.setObject(new File("id-1", "nam1.vid", 1L, Set.of("yo")));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        indexQuery = new IndexQuery();
        indexQuery.setId("id0");
        indexQuery.setObject(new File("id0", "yolo.name0.txt", 0L, Set.of("yo")));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        indexQuery = new IndexQuery();
        indexQuery.setId("id1");
        indexQuery.setObject(new File("id1", "yolo.name1.doc", 1L, Set.of("yo")));
        esTemplate.index(indexQuery, esTemplate.getIndexCoordinatesFor(File.class));
        esTemplate.indexOps(File.class).refresh();

        mockMvc.perform(get("/file?q=name&tags=yo&size=2"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        "{\"total\":3,\"page\":[" +
                                "{\"id\":\"id-1\",\"name\":\"nam1.vid\",\"size\":1,\"tags\":[\"yo\"]}," +
                                "{\"id\":\"id0\",\"name\":\"yolo.name0.txt\",\"size\":0,\"tags\":[\"yo\"]}]}"));
    }
}
