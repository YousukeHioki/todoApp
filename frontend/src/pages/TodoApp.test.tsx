import { test, expect, vi, describe, afterEach, beforeEach } from "vitest";
import { cleanup, render, screen, waitFor } from "@testing-library/react";
import { TodoApp } from "./TodoApp.tsx";
import { userEvent } from "@testing-library/user-event";
import axios from "axios";

describe("TodoApp", () => {
    //テスト前にモックのGETメソッドを動かす
    beforeEach(() => {
        vi.spyOn(axios, "get").mockResolvedValue({ data: [] });
    });

    afterEach(() => {
        cleanup();
    });

    test("testForTest", () => {
        expect(1 + 1).toEqual(2);
    });

    test("render TodoApp screen then see main page", () => {
        render(<TodoApp />);
        expect(screen.getByText("MAIN PAGE")).not.toBeNull();
    });

    test("given getTodoItems has items when render TodoApp then see todo items", async () => {
        const spyGet = vi.spyOn(axios, "get").mockResolvedValue({
            data: [{ pk: "10000", text: "Hello!" }],
        });
        render(<TodoApp />);
        //非同期処理の確認時はwaitForを使う
        await waitFor(() => {
            expect(screen.getByText("Hello!")).not.toBeNull();
            expect(spyGet).toHaveBeenCalledWith("/todo");
        });
    });

    test("add new item when click submit button then send POST request to server", async () => {
        const spyPost = vi.spyOn(axios, "post").mockResolvedValue(undefined);

        render(<TodoApp />);
        await userEvent.type(screen.getByRole("textbox"), "Hello!");
        await userEvent.click(screen.getByRole("button", { name: "submit" }));

        expect(spyPost).toHaveBeenCalledWith("/todo", { text: "Hello!" });
        expect(screen.getByRole("textbox").getAttribute("value")).toEqual("");
    });

    test("add new item when click submit button then get text ", async () => {
        const spyGet = vi
            .spyOn(axios, "get")
            .mockResolvedValueOnce({ data: [] })
            .mockResolvedValueOnce({ data: [{ pk: "10000", text: "Hello!" }] });
        vi.spyOn(axios, "post").mockResolvedValue(undefined);

        render(<TodoApp />);
        await userEvent.type(screen.getByRole("textbox"), "Hello!");
        await userEvent.click(screen.getByRole("button", { name: "submit" }));

        await waitFor(() => {
            expect(spyGet).toHaveBeenNthCalledWith(2, "/todo");
            expect(screen.getByText("Hello!")).not.toBeNull();
        });
    });
});
