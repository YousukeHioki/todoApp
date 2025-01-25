import { useEffect } from "react";

export function TodoApp() {
    useEffect(() => {
        (async () => {
            await getAllTodoItems();
        })();
    }, []);

    async function getAllTodoItems() {
        const fetchResponse: Response = await fetch(
            "http://localhost:8080/todo",
        );
        console.log("fetchResponse", fetchResponse); //OK

        if (fetchResponse.ok) {
            const jsonResponse = await fetchResponse.json();
            console.log("allTodoItems-----", jsonResponse);
        } else {
            console.log("NG-----", fetchResponse.status);
        }
        console.log("最後まできた");
    }

    return (
        <>
            <h1>MAIN PAGE</h1>
        </>
    );
}
