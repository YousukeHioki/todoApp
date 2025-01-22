package com.example.todoapp

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class TodoappApplicationTests {

	@Autowired
	private lateinit var mockMvc: MockMvc

	@Test
	fun contextLoads() {
	}


	@Test
	fun testForTest() {
		assertThat(1+2, equalTo(3))
	}

	@Test
	fun `エンドポイントtodoにPOSTすると 200 OK が返る`(){
		mockMvc.perform(post("/todo"))
			.andExpect(status().isOk)
	}

	@Test
	fun `todoエンドポイントに any をPOSTするとテーブルに any が追加される`() {
		mockMvc.perform(post("/todo").content("{test:\"any\"}"))
	}

	@Test
	fun `id のパスパラメータを渡すと id にあてはまるアイテムの完了状態が返る`() {
		mockMvc.perform(get("/todo/{id}", "12345"))
			.andExpect(status().isOk)
			//データの特定の値を確認する
			.andExpect(content().string("false"))
			//モックの値？が表示されるので"false"が表示されるわけではない
			println(content().string("false"))
	}
}
