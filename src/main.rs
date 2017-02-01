#[macro_use] extern crate conrod;
extern crate piston_window;
extern crate find_folder;
extern crate curl;
extern crate os_type;

use curl::easy::Easy;
use std::io::{stdout, Write};
use std::str;

use conrod::{
    Canvas,
    Colorable,
    Frameable,
    Positionable,
    Text,
    Widget,
    Button,
    DropDownList,
    Labelable,
    Sizeable,
    Slider,
    NumberDialer,
    TextBox,
    XYPad
};
use piston_window::{ UpdateEvent, PressEvent, OpenGL, PistonWindow, WindowSettings };

use conrod::{Theme, Scalar};
use conrod::color;

/// Conrod is backend agnostic. Here, we define the `piston_window` backend to use for our `Ui`.
type Backend = (piston_window::G2dTexture<'static>, piston_window::Glyphs);
type Ui = conrod::Ui<Backend>;
type UiCell<'a> = conrod::UiCell<'a, Backend>;

#[derive(Debug)]
struct Mod {
    name: String,
    description: String,
    author: String,
    code_url: String,
    zip_url: String,
    img_url: String,
    forum_thread: String,
    active: bool
}

impl Mod {
    fn new(name: String, description: String, author: String, code_url: String, zip_url: String, img_url: String, forum_thread: String, active: bool) -> Mod {
        Mod {
            name: name,
            description: description,
            author: author,
            code_url: code_url,
            zip_url: zip_url,
            img_url: img_url,
            forum_thread: forum_thread,
            active: active
        }
    }
}

enum ListEntry {
    Mod {name: String, }
}


fn getModByName(name: String, mods: &Vec<Mod>) -> &Mod {
    return mods.iter().find(|&r| r.name == name).unwrap();
}


const TITLE: &'static str = "Minetest Modinstaller v0.1";

fn main() {
    let mods = load_mod_list();
    println!("{:?}", mods);

    println!("{:?}", get_minetest_path());


    let mut window: PistonWindow = WindowSettings::new(TITLE, (800, 600))
                                    .opengl(OpenGL::V3_2).exit_on_esc(false).vsync(true).build().unwrap();

    let mut ui = {
        let assets = find_folder::Search::KidsThenParents(3, 5)
            .for_folder("assets").unwrap();
        let font_path = assets.join("fonts/NotoSans/NotoSans-Regular.ttf");
        let theme = Theme::default();
        let glyph_cache = piston_window::Glyphs::new(&font_path, window.factory.clone()).unwrap();
        Ui::new(glyph_cache, theme)
    };


    while let Some(event) = window.next() {
        // Button/Mouse events
        if let Some(button) = event.press_args() {
            if button == piston_window::Button::Keyboard(piston_window::Key::LShift) {
                println!("Hallo");
            }
        }

        //Drawing
        ui.handle_event(&event);
        event.update(|_| ui.set_widgets(set_ui));
        window.draw_2d(&event, |c, g| ui.draw(c, g));
    }
}

fn set_ui(ref mut ui: UiCell) {
    widget_ids!{
        CANVAS,
        HEADER,
        BODY,
        LEFT_COLUMN,
        MODS_COLUMN,
        INFO_COLUMN,
        INSTALL_BUTTON_CANVAS,
        INSTALL_BUTTON,
        HEADER_TEXT,
    }

    const PAD: Scalar = 20.0;
    const INSTALL_BUTTON_HEIGHT: f64 = 40.0;

    let header = Canvas::new().color(color::WHITE).pad(PAD).length(100.0);
    let mods_column = Canvas::new().color(color::WHITE).scroll_kids().pad(PAD);
    let info_column = Canvas::new().color(color::WHITE).scroll_kids().pad(PAD);
    let install_button_canvas = Canvas::new().color(color::WHITE).pad(PAD).length(INSTALL_BUTTON_HEIGHT);

    Canvas::new()
    .frame(1.0)
    .color(color::LIGHT_RED)
    .flow_down(&[
         (HEADER, header),
         (BODY, Canvas::new().flow_right(&[
             (LEFT_COLUMN, Canvas::new().flow_down(&[
                (MODS_COLUMN, mods_column),
                (INSTALL_BUTTON_CANVAS,  install_button_canvas)
             ])),
             (INFO_COLUMN, info_column)
         ]))
      ])
    .set(CANVAS, ui);

    Text::new(TITLE)
        .color(color::DARK_GREEN)
        .font_size(40)
        .middle_of(HEADER)
        .align_text_middle()
        .line_spacing(2.5)
        .set(HEADER_TEXT, ui);

    Button::new()
        .w_h(1000.0, INSTALL_BUTTON_HEIGHT)
        .middle_of(INSTALL_BUTTON_CANVAS)
        .label(&"Install".to_string())
        .label_font_size(22)
        .color(color::LIGHT_GREEN)
        .react(|| {
            println!("Install");
        })
        .set(INSTALL_BUTTON, ui);
}


fn get_minetest_path() -> String {
    use std::env;

    match os_type::current_platform() {
        os_type::OSType::OSX => env::var("HOME").unwrap() + "/Library/Application Support/minetest",
        os_type::OSType::Windows => env::var("APPDATA").unwrap() + "/minetest",
        _ => env::var("HOME").unwrap() + "./minetest"
    }
}

fn get_mod_path_by_name(name: String) -> String {
    get_minetest_path() + "/mods/" + &name
}

fn load_mod_list() -> Vec<Mod> {
    let mut mods: Vec<Mod> = Vec::new();

    let mut buf = Vec::new();
    let mut handle = Easy::new();
    handle.url("https://raw.githubusercontent.com/DevsWithoutHobbies/minetest-modinstaller-data/master/index").unwrap();

    {
        let mut transfer = handle.transfer();
        transfer.write_function(|data| {
            buf.extend_from_slice(data);
            Ok(data.len())
        }).unwrap();
        transfer.perform().unwrap();
    }

    let mod_data_string = str::from_utf8(&buf).unwrap().to_string();
    let all_mod_data = mod_data_string.split("\n");
    for raw_mod_data in all_mod_data {
        if raw_mod_data.len() == 0 {continue;}
        let mod_data: Vec<&str> = raw_mod_data.split(":::").collect();

        mods.push(Mod::new(mod_data[0].to_string(), mod_data[6].to_string(), mod_data[1].to_string(), mod_data[3].to_string(), mod_data[4].to_string(), mod_data[5].to_string(), mod_data[2].to_string(), false));
    }

    mods
}

// fn download_mod_from(name: String, zip_url: String) -> Vec<String> {
//
// }
