import { useEffect, useState } from "react";
const VITE_API_URL = import.meta.env.VITE_API_URL;

import axios from "axios";

interface TodoItem {
    pk: string;
    text: string;
}

export function TodoApp() {
    const [allTodoItems, setAllTodoItems] = useState<TodoItem[]>([]);
    const [pk, setPk] = useState<string>("");
    const [text, setText] = useState<string>("");
    const [isReadOnly, setIsReadOnly] = useState<boolean>(true);
    //全てのTodoItemsをページを開いた時に取得する
    useEffect(() => {
        (async () => {
            await getAllTodoItems();
        })();
    }, []);
    //allTodoItemsに状態保持していることの確認用
    useEffect(() => {
        console.log("allTodoItems-----状態保持:", allTodoItems);
    }, [allTodoItems]);
    useEffect(() => {
        axios.get("/todo").then((response) => {
            setAllTodoItems(response.data);
        });
    }, []);

    //全てのTodoItemsを取得する関数
    async function getAllTodoItems() {
        const fetchResponse: Response = await fetch(VITE_API_URL + "/todo");
        if (fetchResponse.ok) {
            const jsonResponse = await fetchResponse.json();
            setAllTodoItems(jsonResponse);
            console.log("allTodoItems-----fetch時:", jsonResponse);
        } else {
            console.log("NG status code-----", fetchResponse.status);
        }
    }
    //新しいTodoItemを登録
    function addNewTodoItem() {
        axios.post("/todo", { text: text }).then(() => {
            axios.get("/todo").then((response) => {
                console.log("response.data-----", response.data);
                setAllTodoItems(response.data);
            });
        });
        setText("");
    }
    //編集モードに変更
    function changeToEditMode(selectedPk: string, selectedText: string) {
        setIsReadOnly(false);
        setPk(selectedPk);
        setText(selectedText);
    }
    //TodoItemを編集
    function editTodoItem(selectedPk: string) {
        axios.put(`/todo/${selectedPk}`, { text: text }).then((response) => {
            console.log("response.data-----", response.data);
            //TODO:textに完了の文字列が入るか確認
            // alert(response.data.text)
        });
    }
    //TodoItemを削除
    function deleteTodoItem(selectedPk: string) {
        axios.delete(`/todo/${selectedPk}`).then((response) => {
            console.log("response.data-----", response.data);
            //TODO:textに完了の文字列が入るか確認
            // alert(response.data.text)
        });
    }

    return (
        <>
            <h1>TODO-LIST</h1>

            {allTodoItems.map((todoItem, index) => {
                return (
                    <>
                        <div
                            key={index}
                            id="card"
                            style={{
                                backgroundColor: "lightcyan",
                                border: "2px solid lightgray",
                                borderRadius: "10px",
                                padding: "10px 20px",
                                margin: "10px",
                            }}
                        >
                            <h2>{index + 1}番目のデータ</h2>
                            <input value={todoItem.pk} />
                            <input
                                style={{
                                    fontSize: "large",
                                }}
                                value={todoItem.text}
                                readOnly={isReadOnly}
                                onChange={(e) => {
                                    setText(e.target.value);
                                }}
                            />
                            <div>
                                {isReadOnly ? (
                                    // 読み取り専用モードの時のボタン[削除・編集・完了]
                                    <>
                                        <button
                                            onClick={() => {
                                                deleteTodoItem(todoItem.pk);
                                            }}
                                        >
                                            Delete
                                        </button>
                                        <button
                                            onClick={() =>
                                                changeToEditMode(
                                                    todoItem.pk,
                                                    todoItem.text,
                                                )
                                            }
                                        >
                                            Edit
                                        </button>
                                        <button>Done</button>
                                    </>
                                ) : (
                                    // 編集モードの時のボタン[キャンセル・登録]
                                    <>
                                        <button
                                            onClick={() => setIsReadOnly(true)}
                                        >
                                            Cancel
                                        </button>
                                        <button
                                            onClick={() => {
                                                editTodoItem(pk);
                                            }}
                                        >
                                            Submit
                                        </button>
                                    </>
                                )}
                            </div>
                        </div>
                    </>
                );
            })}
            <div>
                <input
                    type="text"
                    value={text}
                    onChange={(e) => setText(e.target.value)}
                />
                <button
                    onClick={async () => {
                        await addNewTodoItem();
                    }}
                >
                    submit
                </button>
            </div>
        </>
    );
}
