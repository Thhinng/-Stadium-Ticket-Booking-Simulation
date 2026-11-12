package view;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.NoSuchElementException;

public class ConsoleView {
    private final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

    public String ask(String prompt) {
        System.out.print(prompt);
        System.out.flush();
        try {
            String value = this.reader.readLine();
            if (value == null) throw new NoSuchElementException("Đã kết thúc nhập liệu.");
            return value.trim();
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể đọc dữ liệu từ bàn phím.", ex);
        }
    }

    /**
     * Nhập mật khẩu mà không hiện ký tự thật trong Terminal.
     * Trên Windows: in dấu * khi gõ (dùng PowerShell có sẵn).
     * Trên các hệ điều hành khác: Console.readPassword() ẩn hoàn toàn ký tự.
     */
    public String askPassword(String prompt) {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            String value = this.askPasswordWithStars(prompt);
            if (value != null) {
                return value;
            }
        }

        Console terminal = System.console();
        if (terminal != null) {
            char[] chars = terminal.readPassword("%s", prompt);
            if (chars == null) {
                throw new NoSuchElementException("Đã kết thúc nhập mật khẩu.");
            }
            try {
                return new String(chars);
            } finally {
                Arrays.fill(chars, '\0');
            }
        }

        // Chỉ dùng khi đầu vào được chuyển qua pipe, ví dụ chạy test tự động.
        // Nếu đang dùng Terminal Windows thì không cho nhập mật khẩu hiển thị rõ.
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            throw new IllegalStateException(
                    "Không bật được chế độ nhập mật khẩu ẩn. Hãy chạy trong Terminal của VS Code hoặc PowerShell.");
        }
        return this.ask(prompt);
    }

    /**
     * Java Console chỉ hỗ trợ ẩn hoàn toàn; đoạn này giúp Windows hiện dấu *.
     * Dữ liệu mật khẩu không được đưa vào tham số dòng lệnh hay lịch sử PowerShell.
     */
    private String askPasswordWithStars(String prompt) {
        String script =
                "$ErrorActionPreference = 'Stop'\n" +
                "$password = New-Object System.Text.StringBuilder\n" +
                "while ($true) {\n" +
                "  $key = [Console]::ReadKey($true)\n" +
                "  if ($key.Key -eq [ConsoleKey]::Enter) { [Console]::Error.WriteLine(); break }\n" +
                "  if ($key.Key -eq [ConsoleKey]::Backspace) {\n" +
                "    if ($password.Length -gt 0) {\n" +
                "      $password.Length = $password.Length - 1\n" +
                "      [Console]::Error.Write([string][char]8 + ' ' + [string][char]8)\n" +
                "    }\n" +
                "  } elseif (-not [char]::IsControl($key.KeyChar)) {\n" +
                "    [void]$password.Append($key.KeyChar)\n" +
                "    [Console]::Error.Write('*')\n" +
                "  }\n" +
                "}\n" +
                "[Console]::Out.WriteLine([Convert]::ToBase64String(" +
                "[Text.Encoding]::UTF8.GetBytes($password.ToString())))\n";

        String encodedScript = Base64.getEncoder().encodeToString(
                script.getBytes(StandardCharsets.UTF_16LE));

        try {
            ProcessBuilder builder = new ProcessBuilder(
                    "powershell.exe", "-NoProfile", "-EncodedCommand", encodedScript);
            builder.redirectInput(ProcessBuilder.Redirect.INHERIT);
            builder.redirectError(ProcessBuilder.Redirect.INHERIT);
            // Thu stdout (mật khẩu đã mã hóa Base64), chỉ dấu * được hiện ra Terminal.
            System.out.print(prompt);
            System.out.flush();
            Process process = builder.start();
            String encodedPassword;
            try (BufferedReader output = new BufferedReader(new InputStreamReader(
                    process.getInputStream(), StandardCharsets.US_ASCII))) {
                encodedPassword = output.readLine();
            }
            int status = process.waitFor();
            if (status != 0 || encodedPassword == null) {
                return null;
            }
            byte[] passwordBytes = Base64.getDecoder().decode(encodedPassword.trim());
            try {
                return new String(passwordBytes, StandardCharsets.UTF_8);
            } finally {
                Arrays.fill(passwordBytes, (byte) 0);
            }
        } catch (Exception e) {
            // Nếu PowerShell không khởi chạy, dùng Console.readPassword ở trên.
            return null;
        }
    }

    public int askInt(String prompt) {
        while (true) {
            try { return Integer.parseInt(this.ask(prompt)); }
            catch (NumberFormatException ex) { this.error("Vui lòng nhập một số nguyên."); }
        }
    }

    public long askLong(String prompt) {
        while (true) {
            try { return Long.parseLong(this.ask(prompt).replace(",", "").replace(".", "")); }
            catch (NumberFormatException ex) { this.error("Vui lòng nhập số tiền nguyên, ví dụ: 250000."); }
        }
    }

    public boolean confirm(String prompt) {
        while (true) {
            String ans = this.ask(prompt + " (y/n): ").toLowerCase();
            if (ans.equals("y") || ans.equals("yes") || ans.equals("c") || ans.equals("co")) return true;
            if (ans.equals("n") || ans.equals("no") || ans.equals("k") || ans.equals("khong")) return false;
            this.error("Nhập y hoặc n.");
        }
    }

    public void line() { System.out.println("===================================================================="); }
    public void title(String text) { System.out.println(); this.line(); System.out.println(" " + text); this.line(); }
    public void info(String text) { System.out.println("[THÔNG BÁO] " + text); }
    public void success(String text) { System.out.println("[THÀNH CÔNG] " + text); }
    public void error(String text) { System.out.println("[LỖI] " + text); }
}
