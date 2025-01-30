import { useEffect, useState } from "react";
import axios from "axios";

interface TodoItem {
  pk: string;
  text: string;
}

export function TodoApp() {
  const [allTodoItems, setAllTodoItems] = useState<TodoItem[]>([]);
  const [pk, setPk] = useState<string>("");
  const [text, setText] = useState<string>("");
  const [isEditMode, setIsEditMode] = useState<boolean>(false);

  //本番用
  //全てのTodoItemsをページを開いた時に取得する
  // useEffect(() => {
  //   (async () => {
  //     await getAllTodoItems();
  //   })();
  // }, []);
  //allTodoItemsに状態保持していることの確認用
  useEffect(() => {
    console.log("allTodoItems----------", allTodoItems);
  }, [allTodoItems]);
  useEffect(() => {
    getAllTodoItems();
  }, []);

  //全てのTodoItemsを取得する関数
  function getAllTodoItems() {
    axios.get("/todo").then((response) => {
      setAllTodoItems(response.data);
    });
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
    setIsEditMode(true);
    setPk(selectedPk);
    setText(selectedText);
  }
  //TodoItemを更新
  function updateTodoItem(selectedPk: string) {
    axios
      .put(`/todo/${selectedPk}`, { text: text })
      .then((response) => {
        console.log("response------", response.data);
        //TODO:textに完了の文字列が入るか確認
        console.log("text------", response.data.text);
        setText("");
        setIsEditMode(false);
      })
      .catch((error) => {
        console.error("Update error:", error);
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

  //TODO:Doneボタンでの完了フラグ処理関数作成

  return (
    <>
      <h1>TODO-LIST</h1>
      {!isEditMode ? (
        <>
          {allTodoItems.map((todoItem, index) => {
            return (
              <>
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
                  <h2>{index + 1}番目のデータ</h2>
                  <div>PK:{todoItem.pk}</div>
                  <div>{todoItem.text}</div>
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
                    <button>Done</button>
                  </div>
                </div>
              </>
            );
          })}
          {/*テキストエリア・登録ボタン*/}
          <div>
            <input
              type="text"
              value={text}
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
        </>
      ) : (
        // 編集モードの時の選択したcard単体表示
        <div
          style={{
            backgroundColor: "lightgreen",
            border: "2px solid lightgray",
            borderRadius: "10px",
            padding: "10px 20px",
            margin: "10px",
          }}
        >
          <div>{pk}</div>
          <input
            type="text"
            value={text}
            onChange={(e) => setText(e.target.value)}
          />
          <button
            onClick={() => {
              setText("");
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
      )}
    </>
  );
}
