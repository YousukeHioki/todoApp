package com.example.todoapp

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
class TodoappApplicationTests {

	@Autowired
	private lateinit var mockMvc: MockMvc

	@Test
	fun contextLoads() {
	}

	@Test
	fun 最初のテスト() {
		assertThat(1+2, equalTo(3))
	}

	@Test
	fun `エンドポイントtodoにPOSTすると200 OKが返る`(){
		mockMvc.perform(post("/todo"))
			.andExpect(status().isOk)
	}

	@Test
	fun `エンドポイントtodoにfooをPOSTするとテーブルに"foo"が追加される`() {
		mockMvc.perform(post("/todo").content("{test:\"foo\"}"))
	}

}
