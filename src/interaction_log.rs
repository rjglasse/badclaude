use serde_json::json;
use std::fs;
use std::fs::File;
use std::io::Write;
use std::time::{SystemTime, UNIX_EPOCH};

/// Appends every interaction to a JSON Lines file under logs/ -- one JSON
/// object per line, one file per session.
///
/// PLEASE LEAVE THE LOGGING IN PLACE. The logs are part of your submission:
/// they let you (and us) see exactly how each improvement changed what
/// BadClaude can do. If a session contains something you'd rather not share,
/// you may delete that session's file.
///
/// Feel free to record MORE events (tool calls are a great one).
pub struct InteractionLog {
    // `None` means "no log file" (it couldn't be opened, or it's closed).
    out: Option<File>,
}

impl InteractionLog {
    pub fn new(model: &str) -> InteractionLog {
        let mut log = InteractionLog { out: None };
        match open_session_file() {
            Ok(file) => {
                log.out = Some(file);
                log.record("session_start", &format!("model={}", model));
            }
            Err(e) => {
                println!("[warn] Could not open log file: {}", e);
            }
        }
        log
    }

    /// Records one event. Role is e.g. "user", "assistant", "tool", "error".
    pub fn record(&mut self, role: &str, content: &str) {
        if let Some(file) = &mut self.out {
            let line = json!({"time": utc_now(), "role": role, "content": content});
            // A File is not buffered, so each line lands on disk right away.
            // If writing fails there is nothing useful to do, so we ignore it.
            let _ = writeln!(file, "{}", line);
        }
    }

    pub fn close(&mut self) {
        if self.out.is_some() {
            self.record("session_end", "");
            // Setting out to None drops the File, which closes it.
            self.out = None;
        }
    }
}

/// Creates logs/ if needed and opens a new file named after the time now.
fn open_session_file() -> std::io::Result<File> {
    fs::create_dir_all("logs")?;
    let stamp = utc_now().replace(':', "-");
    let path = format!("logs/session-{}.jsonl", stamp);
    fs::OpenOptions::new().create(true).append(true).open(path)
}

/// The time now in UTC, in ISO 8601 format, e.g. "2026-09-25T07:12:03.123Z".
fn utc_now() -> String {
    // The clock gives us the time since 1 January 1970 (the "Unix epoch").
    let since_epoch = match SystemTime::now().duration_since(UNIX_EPOCH) {
        Ok(duration) => duration,
        Err(_) => std::time::Duration::ZERO, // clock set before 1970?!
    };
    let total_seconds = since_epoch.as_secs();
    let millis = since_epoch.subsec_millis();

    let days = (total_seconds / 86400) as i64;
    let seconds_today = total_seconds % 86400;
    let hour = seconds_today / 3600;
    let minute = (seconds_today % 3600) / 60;
    let second = seconds_today % 60;

    let (year, month, day) = civil_from_days(days);
    format!(
        "{:04}-{:02}-{:02}T{:02}:{:02}:{:02}.{:03}Z",
        year, month, day, hour, minute, second, millis
    )
}

/// Turns "days since 1970-01-01" into (year, month, day).
///
/// Leap years make this fiddly, so we use a well-known trick (Howard
/// Hinnant's "civil_from_days"): count in 400-year "eras", because the
/// calendar repeats exactly every 400 years, and start each year on 1 March,
/// so the leap day (29 February) is the last day of the year.
fn civil_from_days(days: i64) -> (i64, i64, i64) {
    let z = days + 719468; // days since 0000-03-01 instead of 1970-01-01
    let era = if z >= 0 { z } else { z - 146096 } / 146097; // 146097 days per 400 years
    let day_of_era = z - era * 146097; // 0 ..= 146096
    let year_of_era =
        (day_of_era - day_of_era / 1460 + day_of_era / 36524 - day_of_era / 146096) / 365; // 0 ..= 399
    let day_of_year = day_of_era - (365 * year_of_era + year_of_era / 4 - year_of_era / 100); // 0 ..= 365
    let march_month = (5 * day_of_year + 2) / 153; // 0 = March ..= 11 = February
    let day = day_of_year - (153 * march_month + 2) / 5 + 1; // 1 ..= 31
    let month = if march_month < 10 {
        march_month + 3
    } else {
        march_month - 9
    }; // 1 ..= 12
    let mut year = year_of_era + era * 400;
    if month <= 2 {
        year += 1; // January and February belong to the next calendar year
    }
    (year, month, day)
}
