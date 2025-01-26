import { useEffect, useState } from "react";
const VITE_API_URL = import.meta.env.VITE_API_URL;

interface TodoItem {
    pk: string;
    text: string;
}

export function TodoApp() {
    const [allTodoItems, setAllTodoItems] = useState<TodoItem[]>([]);
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

    return (
        <>
            <h1>MAIN PAGE</h1>
            {allTodoItems.map((todoItem, index) => {
                return (
                    <>
                        <div key={index}>
                            <h2>{index + 1}番目のデータ</h2>
                            <div>PK: {todoItem.pk}</div>
                            <div>text: {todoItem.text}</div>
                        </div>
                    </>
                );
            })}
        </>
    );
}
