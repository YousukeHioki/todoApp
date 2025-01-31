import { useEffect, useState } from "react";
import axios from "axios";

interface TodoItem {
	pk: string;
	text: string;
}

export function TodoApp() {
	const [allTodoItems, setAllTodoItems] = useState<TodoItem[]>([]);
	const [pageTitle, setPageTitle] = useState<string>("TODO-LIST");
	const [pk, setPk] = useState<string>("");
	const [text, setText] = useState<string>("");
	const [isEditMode, setIsEditMode] = useState<boolean>(false);

	//全てのTodoItemsをページを開いた時に取得する
	useEffect(() => {
		getAllTodoItems();
	}, []);

	//全てのTodoItemsを取得する関数
	function getAllTodoItems() {
		axios
			.get("/todo")
			.then((response) => {
				console.log("get response-------", response);
				setAllTodoItems(response.data);
			})
			.catch((error) => {
				console.error("Get items error:", error);
			});
	}
	//新しいTodoItemを登録
	function addNewTodoItem() {
		if (text !== "") {
			axios.post("/todo", { text: text }).then(() => {
				axios.get("/todo").then((response) => {
					console.log("add response-------", response);
					setAllTodoItems(response.data);
				});
			});
			setText("");
		} else {
			alert(
				"インプットエリアに何か書いてください。\nPlease write something in the input area.",
			);
		}
	}
	//TodoItemを更新
	function updateTodoItem(selectedPk: string) {
		axios
			.put(`/todo/${selectedPk}`, { text: text })
			.then(() => {
				setText("");
				setPageTitle("TODO-LIST");
				setIsEditMode(false);
			})
			.then(() => {
				axios.get("/todo").then((response) => setAllTodoItems(response.data));
			})
			.catch((error) => {
				console.error("Update error:", error);
			});
		//update the data here
	}
	//TodoItemを削除
	function deleteTodoItem(selectedPk: string) {
		axios.delete(`/todo/${selectedPk}`).then((response) => {
			console.log("delete response-------", response);
		});
	}
	//編集モードに変更
	function changeToEditMode(selectedPk: string, selectedText: string) {
		setIsEditMode(true);
		setPageTitle("What do yo do?");
		setPk(selectedPk);
		setText(selectedText);
	}

	//TODO:Doneボタンでの完了フラグ処理関数作成

	return (
		<div
			style={{
				width: "50vw",
			}}
		>
			<div>testです</div>
			<h1>{pageTitle}</h1>
			{!isEditMode ? (
				<div>
					<div
						style={{
							height: "50vh",
							overflow: "scroll",
						}}
					>
						{allTodoItems.map((todoItem, index) => {
							return (
								<div>
									<div
										key={index}
										style={{
											backgroundColor: "lightcyan",
											border: "2px solid lightgray",
											borderRadius: "10px",
											padding: "10px 20px",
											margin: "10px",
										}}
									>
										<h2>TODO - No.{index + 1}</h2>
										{/*<div>PK:{todoItem.pk}</div>*/}
										<div
											style={{
												margin: "5px",
												fontSize: "larger",
												fontWeight: "bold",
												color: "navy",
											}}
										>
											{todoItem.text}
										</div>
										<div
											style={{
												display: "flex",
												justifyContent: "space-evenly",
											}}
										>
											{/*ボタンエリア*/}
											<button
												onClick={() => {
													deleteTodoItem(todoItem.pk);
												}}
											>
												Delete
											</button>
											<button
												onClick={() =>
													changeToEditMode(todoItem.pk, todoItem.text)
												}
											>
												Edit
											</button>
											{/*TODO:Doneボタンでの完了フラグ処理関数作成*/}
											{/*<button>Done</button>*/}
										</div>
									</div>
								</div>
							);
						})}
					</div>
					{/*テキストエリア・登録ボタン*/}
					<div
						style={{
							width: "100%",
							display: "flex",
							justifyContent: "space-evenly",
							margin: "10px auto",
						}}
					>
						<input
							type="text"
							value={text}
							placeholder="add new todo"
							style={{
								width: "60%",
							}}
							onChange={(e) => setText(e.target.value)}
						/>
						<button
							onClick={() => {
								addNewTodoItem();
							}}
						>
							submit
						</button>
					</div>
				</div>
			) : (
				// 編集モードの時の選択したcard単体表示
				<div
					style={{
						width: "100%",
						backgroundColor: "lightgreen",
						border: "2px solid lightgray",
						borderRadius: "10px",
						padding: "50px 20px",
						margin: "auto",
					}}
				>
					{/*<div>{pk}</div>*/}
					<div>Todo</div>
					<input
						type="text"
						value={text}
						style={{
							width: "90%",
							margin: "10px auto 30px",
							padding: "3px",
						}}
						onChange={(e) => setText(e.target.value)}
					/>
					<div
						style={{
							display: "flex",
							justifyContent: "space-evenly",
						}}
					>
						<button
							onClick={() => {
								setText("");
								setPageTitle("TODO-LIST");
								setIsEditMode(false);
							}}
						>
							Cancel
						</button>
						<button
							onClick={() => {
								updateTodoItem(pk);
							}}
						>
							Submit
						</button>
					</div>
				</div>
			)}
		</div>
	);
}
