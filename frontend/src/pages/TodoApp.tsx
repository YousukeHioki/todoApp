import { useEffect, useState } from "react";

const VITE_API_URL = import.meta.env.VITE_API_URL;
export function TodoApp() {
    const [allTodoItems, setAllTodoItems] = useState([]);
    //全てのTodoItemsをページを開いた時に取得する
    useEffect(() => {
        (async () => {
            await getAllTodoItems();
        })();
    }, []);
    //全てのTodoItemsを取得する関数
    async function getAllTodoItems() {
        const fetchResponse: Response = await fetch(VITE_API_URL + "/todo");
        console.log("fetchResponse", fetchResponse); //OK

        if (fetchResponse.ok) {
            const jsonResponse = await fetchResponse.json();
            setAllTodoItems(jsonResponse);
            console.log("allTodoItems-----fetch時:", jsonResponse);
            console.log("allTodoItems-----状態保持:", allTodoItems);
        } else {
            console.log("NG status code-----", fetchResponse.status);
        }
        console.log("最後まできた");
    }

    return (
        <>
            <h1>MAIN PAGE</h1>
        </>
    );
}
