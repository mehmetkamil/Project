#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
📊 Sigorta Rapor ve Kayıt Yönetimi - Profesyonel GUI
=======================================================
Özellikler:
- SQLite veritabanı entegrasyonu
- PDF otomatik okuma ve veri çıkarma
- Excel raporlama ve dışa aktarma
- Gelişmiş filtreleme ve arama
- İstatistik ve analiz
- Modern ve kullanıcı dostu arayüz
"""

import tkinter as tk
from tkinter import ttk, messagebox, filedialog
import os
import sys
import sqlite3
import pandas as pd
from datetime import datetime
from pathlib import Path
import subprocess

# All.py modülünden fonksiyonları import et
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

try:
    from All import (
        init_database,
        insert_police_data,
        identify_policy_type,
        process_hdi_trafik,
        hdi_yeni_police,
        process_ray_trafik,
        process_ethica_trafik,
        process_sompo_trafik,
        process_quick_trafik,
        process_doga_trafik,
        process_hepiyi_trafik,
        process_allianz_trafik,
        process_allianz_kasko,
        process_seyahat,
        process_isyeri,
        process_nakliyat,
        process_konut,
        process_saglik,
        process_dask,
        process_vehicle
    )
    TAYYARE_AVAILABLE = True
except ImportError as e:
    print(f"⚠️ Uyarı: All.py modülü bulunamadı: {e}")
    TAYYARE_AVAILABLE = False

# Renkler - Muhasebe GUI ile uyumlu
COLORS = {
    'primary': '#2563eb',
    'success': '#16a34a',
    'warning': '#ea580c',
    'danger': '#dc2626',
    'info': '#0891b2',
    'bg_light': '#f8fafc',
    'text_dark': '#1e293b',
    'text_light': '#ffffff'
}


class DatabaseManager:
    """Veritabanı yönetimi sınıfı"""
    
    def __init__(self, db_path=None):
        # Tayyare2.py ile uyumlu yol kullan
        if db_path is None:
            script_dir = os.path.dirname(os.path.abspath(__file__))
            excel_klasoru = os.path.join(script_dir, 'excel_klasoru')
            os.makedirs(excel_klasoru, exist_ok=True)
            db_path = os.path.join(excel_klasoru, 'POLICELER.db')
        self.db_path = db_path
        init_database(self.db_path) if TAYYARE_AVAILABLE else None
        
        # Session başlangıcındaki max ID'yi kaydet (yeni kayıtları tespit için)
        self.session_start_max_id = self.get_max_id()
    
    def get_max_id(self):
        """Veritabanındaki maksimum ID'yi getir"""
        try:
            conn = sqlite3.connect(self.db_path)
            cursor = conn.cursor()
            cursor.execute("SELECT MAX(id) FROM policeler")
            result = cursor.fetchone()[0]
            conn.close()
            return result if result else 0
        except:
            return 0
    
    def get_all_records(self):
        """Tüm kayıtları getir - Yeniler en üstte"""
        try:
            conn = sqlite3.connect(self.db_path)
            # ID'ye göre AZALAN sıralama - en yeni ID'ler en üstte
            df = pd.read_sql_query("SELECT * FROM policeler ORDER BY id DESC", conn)
            conn.close()
            
            # Sütun isimlerini büyük harfe çevir
            df.columns = df.columns.str.upper()
            
            return df
        except Exception as e:
            print(f"Kayıt getirme hatası: {e}")
            return pd.DataFrame()
    
    def search_records(self, **filters):
        """Filtreye göre kayıt ara"""
        try:
            conn = sqlite3.connect(self.db_path)
            
            query = "SELECT * FROM policeler WHERE 1=1"
            params = []
            
            if filters.get('sigortali'):
                query += " AND sigortali LIKE ?"
                params.append(f"%{filters['sigortali']}%")
            
            if filters.get('police_no'):
                query += " AND police_no LIKE ?"
                params.append(f"%{filters['police_no']}%")
            
            if filters.get('tur'):
                query += " AND tur = ?"
                params.append(filters['tur'])
            
            if filters.get('sirket'):
                query += " AND sirket = ?"
                params.append(filters['sirket'])
            
            if filters.get('plaka'):
                query += " AND plaka LIKE ?"
                params.append(f"%{filters['plaka']}%")
            
            # Yeniler en üstte - ID'ye göre azalan sıralama
            query += " ORDER BY id DESC"
            
            df = pd.read_sql_query(query, conn, params=params)
            conn.close()
            
            # Sütun isimlerini büyük harfe çevir
            df.columns = df.columns.str.upper()
            
            return df
        except Exception as e:
            print(f"Arama hatası: {e}")
            return pd.DataFrame()
    
    def delete_record(self, record_id):
        """Kayıt sil"""
        try:
            conn = sqlite3.connect(self.db_path)
            cursor = conn.cursor()
            cursor.execute("DELETE FROM policeler WHERE id = ?", (record_id,))
            conn.commit()
            conn.close()
            return True
        except Exception as e:
            print(f"Silme hatası: {e}")
            return False
    
    def update_record(self, record_id, **fields):
        """Kayıt güncelle"""
        try:
            conn = sqlite3.connect(self.db_path)
            cursor = conn.cursor()
            
            updates = []
            params = []
            for key, value in fields.items():
                updates.append(f"{key} = ?")
                params.append(value)
            
            params.append(record_id)
            query = f"UPDATE policeler SET {', '.join(updates)} WHERE id = ?"
            
            cursor.execute(query, params)
            conn.commit()
            conn.close()
            return True
        except Exception as e:
            print(f"Güncelleme hatası: {e}")
            return False
    
    def get_statistics(self):
        """İstatistikleri getir"""
        try:
            conn = sqlite3.connect(self.db_path)
            cursor = conn.cursor()
            
            # Toplam kayıt
            cursor.execute("SELECT COUNT(*) FROM policeler")
            total_count = cursor.fetchone()[0]
            
            # Şirket bazlı dağılım
            cursor.execute("SELECT sirket, COUNT(*) FROM policeler GROUP BY sirket")
            company_stats = cursor.fetchall()
            
            # Tür bazlı dağılım
            cursor.execute("SELECT tur, COUNT(*) FROM policeler GROUP BY tur")
            type_stats = cursor.fetchall()
            
            # Bu ay kayıtlar
            cursor.execute("""
                SELECT COUNT(*) FROM policeler 
                WHERE strftime('%Y-%m', kayit_tarihi) = strftime('%Y-%m', 'now')
            """)
            this_month = cursor.fetchone()[0]
            
            conn.close()
            
            return {
                'total': total_count,
                'company': company_stats,
                'type': type_stats,
                'this_month': this_month
            }
        except Exception as e:
            print(f"İstatistik hatası: {e}")
            return {'total': 0, 'company': [], 'type': [], 'this_month': 0}


class RaporGUI:
    """Ana GUI Sınıfı"""
    
    def __init__(self, root):
        self.root = root
        self.root.title("📊 Sigorta Rapor ve Kayıt Yönetimi")
        self.root.geometry("1600x900")
        
        # Veritabanı yöneticisi
        self.db_manager = DatabaseManager()
        
        # Arama filtreleri
        self.search_filters = {}
        
        # UI oluştur
        self.create_ui()
        
        # Başlangıç
        self.log("✅ Uygulama başlatıldı", "success")
        self.load_data()
        self.update_statistics()
    
    def create_ui(self):
        """Kullanıcı arayüzünü oluştur"""
        # Menü
        self.create_menu()
        
        # Başlık
        self.create_header()
        
        # Ana içerik
        self.create_content()
        
        # Durum çubuğu
        self.create_status_bar()
    
    def create_menu(self):
        """Menü çubuğu"""
        menubar = tk.Menu(self.root)
        self.root.config(menu=menubar)
        
        # Dosya
        file_menu = tk.Menu(menubar, tearoff=0)
        menubar.add_cascade(label="📁 Dosya", menu=file_menu)
        file_menu.add_command(label="PDF Klasörü İşle", command=self.process_pdf_folder)
        file_menu.add_command(label="Excel'e Aktar", command=self.export_to_excel)
        file_menu.add_command(label="Excel Raporunu Aç", command=self.open_excel_report)
        file_menu.add_separator()
        file_menu.add_command(label="Veritabanı Yedekle", command=self.backup_database)
        file_menu.add_separator()
        file_menu.add_command(label="Çıkış", command=self.root.quit)
        
        # Düzenle
        edit_menu = tk.Menu(menubar, tearoff=0)
        menubar.add_cascade(label="✏️ Düzenle", menu=edit_menu)
        edit_menu.add_command(label="Yeni Kayıt Ekle", command=self.add_manual_entry)
        edit_menu.add_command(label="Seçili Kaydı Düzenle", command=self.edit_selected)
        edit_menu.add_command(label="Seçili Kaydı Sil", command=self.delete_selected)
        
        # Görünüm
        view_menu = tk.Menu(menubar, tearoff=0)
        menubar.add_cascade(label="👁️ Görünüm", menu=view_menu)
        view_menu.add_command(label="Yenile", command=self.load_data)
        view_menu.add_command(label="Filtreleri Temizle", command=self.clear_filters)
        view_menu.add_command(label="Logları Temizle", command=self.clear_logs)
        
        # Raporlar
        reports_menu = tk.Menu(menubar, tearoff=0)
        menubar.add_cascade(label="📊 Raporlar", menu=reports_menu)
        reports_menu.add_command(label="Şirket Bazlı Rapor", command=self.show_company_report)
        reports_menu.add_command(label="Tür Bazlı Rapor", command=self.show_type_report)
        reports_menu.add_command(label="Aylık Özet", command=self.show_monthly_summary)
        
        # Yardım
        help_menu = tk.Menu(menubar, tearoff=0)
        menubar.add_cascade(label="❓ Yardım", menu=help_menu)
        help_menu.add_command(label="Kullanım Kılavuzu", command=self.show_help)
        help_menu.add_command(label="Hakkında", command=self.show_about)
    
    def create_header(self):
        """Başlık bölümü"""
        header = tk.Frame(self.root, bg=COLORS['primary'], height=50)
        header.pack(fill=tk.X)
        header.pack_propagate(False)
        
        # Sağ tarafa artı butonu (Manuel Kayıt Ekle)
        add_btn = tk.Button(
            header,
            text="➕",
            font=('Arial', 12, 'bold'),
            bg=COLORS['success'],
            fg='white',
            relief=tk.FLAT,
            padx=10,
            pady=2,
            cursor='hand2',
            command=self.add_inline_record
        )
        add_btn.pack(side=tk.RIGHT, padx=10, pady=8)
        
        # Ana başlık
        title = tk.Label(
            header,
            text="📊 SİGORTA RAPOR VE KAYIT YÖNETİMİ",
            font=('Arial', 14, 'bold'),
            bg=COLORS['primary'],
            fg='white'
        )
        title.pack(pady=5)
        
        # Alt başlık
        subtitle = tk.Label(
            header,
            text="Otomatik PDF Okuma ve Veritabanı Yönetim Sistemi",
            font=('Arial', 9),
            bg=COLORS['primary'],
            fg='white'
        )
        subtitle.pack()
    
    def create_content(self):
        """Ana içerik alanı"""
        main_frame = tk.Frame(self.root, bg=COLORS['bg_light'])
        main_frame.pack(fill=tk.BOTH, expand=True, padx=10, pady=10)
        
        # Sol panel - Filtreler ve butonlar
        left_panel = tk.Frame(main_frame, bg='white', width=175)
        left_panel.pack(side=tk.LEFT, fill=tk.Y, padx=(0, 10))
        left_panel.pack_propagate(False)
        
        # Filtre paneli
        self.create_filter_panel(left_panel)
        
        # Butonlar
        self.create_buttons(left_panel)
        
        # İstatistikler
        self.create_statistics_panel(left_panel)
        
        # Sağ panel - Tablo ve log
        right_panel = tk.Frame(main_frame, bg=COLORS['bg_light'])
        right_panel.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        
        # Tablo
        self.create_table(right_panel)
        
        # Log
        self.create_log_panel(right_panel)
    
    def create_filter_panel(self, parent):
        """Filtre paneli"""
        filter_frame = tk.LabelFrame(
            parent,
            text="🔍 FİLTRELER",
            font=('Arial', 9, 'bold'),
            bg='white',
            fg=COLORS['primary']
        )
        filter_frame.pack(fill=tk.X, padx=5, pady=5)
        
        # Sigortalı
        tk.Label(filter_frame, text="Sigortalı:", font=('Arial', 8), bg='white').pack(anchor='w', padx=5, pady=(5, 0))
        self.filter_sigortali = tk.Entry(filter_frame, font=('Arial', 8))
        self.filter_sigortali.pack(fill=tk.X, padx=5, pady=(1, 3))
        
        # Poliçe No
        tk.Label(filter_frame, text="Poliçe No:", font=('Arial', 8), bg='white').pack(anchor='w', padx=5, pady=(2, 0))
        self.filter_police = tk.Entry(filter_frame, font=('Arial', 8))
        self.filter_police.pack(fill=tk.X, padx=5, pady=(1, 3))
        
        # Tür
        tk.Label(filter_frame, text="Tür:", font=('Arial', 8), bg='white').pack(anchor='w', padx=5, pady=(2, 0))
        self.filter_tur = ttk.Combobox(
            filter_frame, 
            font=('Arial', 8),
            values=['Tümü', 'TRAFİK', 'KASKO', 'DASK', 'EVİM', 'SAĞLIK', 'SEYAHAT', 'NAKLİYAT', 'İŞYERİ'],
            state='readonly'
        )
        self.filter_tur.set('Tümü')
        self.filter_tur.pack(fill=tk.X, padx=5, pady=(1, 3))
        
        # Şirket
        tk.Label(filter_frame, text="Şirket:", font=('Arial', 8), bg='white').pack(anchor='w', padx=5, pady=(2, 0))
        self.filter_sirket = ttk.Combobox(
            filter_frame,
            font=('Arial', 8),
            values=['Tümü', 'ALLIANZ', 'AXA', 'HDI', 'RAY', 'ETHİCA', 'SOMPO', 'QUICK', 'DOĞA', 'HEPİYİ'],
            state='readonly'
        )
        self.filter_sirket.set('Tümü')
        self.filter_sirket.pack(fill=tk.X, padx=5, pady=(1, 3))
        
        # Plaka
        tk.Label(filter_frame, text="Plaka:", font=('Arial', 8), bg='white').pack(anchor='w', padx=5, pady=(2, 0))
        self.filter_plaka = tk.Entry(filter_frame, font=('Arial', 8))
        self.filter_plaka.pack(fill=tk.X, padx=5, pady=(1, 5))
        
        # Arama butonu
        search_btn = tk.Button(
            filter_frame,
            text="🔍 ARA",
            command=self.apply_filters,
            font=('Arial', 8, 'bold'),
            bg=COLORS['primary'],
            fg='white',
            relief=tk.FLAT,
            cursor='hand2',
            pady=4
        )
        search_btn.pack(fill=tk.X, padx=5, pady=(2, 5))
    
    def create_buttons(self, parent):
        """Butonlar"""
        btn_frame = tk.LabelFrame(
            parent,
            text="⚡ İŞLEMLER",
            font=('Arial', 9, 'bold'),
            bg='white'
        )
        btn_frame.pack(fill=tk.X, padx=5, pady=5)
        
        buttons = [
            ("📂 PDF Klasörü İşle", self.process_pdf_folder, '#2563eb'),
            ("➕ Yeni Kayıt Ekle", self.add_manual_entry, '#16a34a'),
            ("💾 Excel'e Aktar", self.export_to_excel, '#ea580c'),
            ("📊 Excel Raporunu Aç", self.open_excel_report, '#8b5cf6'),
            ("🔄 Yenile", self.load_data, '#0891b2'),
            (" Tüm Kayıtları Sil", self.delete_all_records, COLORS['danger'])
        ]
        
        for text, command, color in buttons:
            btn = tk.Button(
                btn_frame,
                text=text,
                command=command,
                font=('Arial', 8, 'bold'),
                bg=color,
                fg='white',
                relief=tk.FLAT,
                cursor='hand2',
                padx=5,
                pady=4
            )
            btn.pack(fill=tk.X, padx=5, pady=2)
    
    def create_statistics_panel(self, parent):
        """İstatistikler paneli"""
        stats_frame = tk.LabelFrame(
            parent,
            text="📈 İSTATİSTİKLER",
            font=('Arial', 9, 'bold'),
            bg='white'
        )
        stats_frame.pack(fill=tk.BOTH, expand=True, padx=5, pady=5)
        
        self.stats_text = tk.Text(
            stats_frame,
            font=('Consolas', 7),
            bg='#f8fafc',
            fg='#1e293b',
            relief=tk.FLAT,
            wrap=tk.WORD
        )
        self.stats_text.pack(fill=tk.BOTH, expand=True, padx=5, pady=5)
    
    def create_table(self, parent):
        """Kayıt tablosu"""
        table_frame = tk.Frame(parent, bg='white')
        table_frame.pack(fill=tk.BOTH, expand=True, pady=(0, 10))
        
        # Scrollbar
        scrollbar_y = ttk.Scrollbar(table_frame, orient=tk.VERTICAL)
        scrollbar_x = ttk.Scrollbar(table_frame, orient=tk.HORIZONTAL)
        
        # Treeview
        columns = ('ID', 'SİGORTALI', 'TARİH', 'MÜŞTERİ_NO', 'POLİÇE_NO', 'TÜR', 
                   'ŞİRKET', 'PLAKA', 'MARKA', 'TUTAR', 'AÇIKLAMA', 'REFERANS', 'İLETİŞİM')
        
        self.tree = ttk.Treeview(
            table_frame,
            columns=columns,
            show='headings',
            yscrollcommand=scrollbar_y.set,
            xscrollcommand=scrollbar_x.set,
            selectmode='extended'
        )
        
        # Sütun başlıkları - BÜYÜK HARFLE
        headers = {
            'ID': 'SIRA',
            'SİGORTALI': 'SİGORTALI',
            'TARİH': 'TARİH',
            'MÜŞTERİ_NO': 'MÜŞTERİ NO',
            'POLİÇE_NO': 'POLİÇE NO',
            'TÜR': 'TÜR',
            'ŞİRKET': 'ŞİRKET',
            'PLAKA': 'PLAKA',
            'MARKA': 'MARKA',
            'TUTAR': 'TUTAR',
            'AÇIKLAMA': 'AÇIKLAMA',
            'REFERANS': 'REFERANS',
            'İLETİŞİM': 'İLETİŞİM'
        }
        
        for col in columns:
            self.tree.heading(col, text=headers[col], command=lambda c=col: self.sort_column(c))
        
        # Sütun genişlikleri - Excel'deki gibi
        self.tree.column('ID', width=50, anchor='center')
        self.tree.column('SİGORTALI', width=250, anchor='w')
        self.tree.column('TARİH', width=90, anchor='center')
        self.tree.column('MÜŞTERİ_NO', width=110, anchor='center')
        self.tree.column('POLİÇE_NO', width=110, anchor='center')
        self.tree.column('TÜR', width=85, anchor='center')
        self.tree.column('ŞİRKET', width=70, anchor='center')
        self.tree.column('PLAKA', width=90, anchor='center')
        self.tree.column('MARKA', width=140, anchor='w')
        self.tree.column('TUTAR', width=110, anchor='e')
        self.tree.column('AÇIKLAMA', width=90, anchor='center')
        self.tree.column('REFERANS', width=90, anchor='center')
        self.tree.column('İLETİŞİM', width=90, anchor='center')
        
        # Scrollbar'ları yerleştir
        scrollbar_y.pack(side=tk.RIGHT, fill=tk.Y)
        scrollbar_x.pack(side=tk.BOTTOM, fill=tk.X)
        self.tree.pack(fill=tk.BOTH, expand=True)
        
        scrollbar_y.config(command=self.tree.yview)
        scrollbar_x.config(command=self.tree.xview)
        
        # Yeni kayıtlar için açık yeşil tema tag'i
        self.tree.tag_configure('yeni_kayit', background='#b8e6b8')
        # Yeni satır için mavi tag
        self.tree.tag_configure('new_row', background='#e8f4fd')
        
        # Inline editing için değişkenler
        self.editing_new_row = False
        self.new_row_item = None
        self.current_edit_entry = None
        self.new_row_data = {}
        
        # Sağ tık menü
        self.create_context_menu()
        self.tree.bind('<Button-3>', self.show_context_menu)
        # Çift tıklama - sadece düzenlenebilir sütunlar için
        self.tree.bind('<Double-Button-1>', self.on_tree_double_click)
    
    def create_log_panel(self, parent):
        """Log paneli"""
        log_frame = tk.LabelFrame(
            parent,
            text="📋 KAYITLAR",
            font=('Arial', 10, 'bold'),
            bg='white'
        )
        log_frame.pack(fill=tk.X)
        
        self.log_text = tk.Text(
            log_frame,
            height=8,
            font=('Consolas', 9),
            bg='#fafafa',
            fg='#374151',
            wrap=tk.WORD
        )
        self.log_text.pack(fill=tk.BOTH, expand=True, padx=5, pady=5)
        
        # Tag'ler
        self.log_text.tag_config('success', foreground='#16a34a')
        self.log_text.tag_config('error', foreground='#dc2626')
        self.log_text.tag_config('warning', foreground='#ea580c')
        self.log_text.tag_config('info', foreground='#2563eb')
    
    def create_status_bar(self):
        """Durum çubuğu"""
        self.status_bar = tk.Label(
            self.root,
            text="Hazır",
            font=('Arial', 9),
            bg='#e2e8f0',
            fg='#475569',
            anchor='w',
            padx=10
        )
        self.status_bar.pack(side=tk.BOTTOM, fill=tk.X)
    
    def create_context_menu(self):
        """Sağ tık menüsü"""
        self.context_menu = tk.Menu(self.root, tearoff=0)
        self.context_menu.add_command(label="️ Sil", command=self.delete_selected)
    
    def show_context_menu(self, event):
        """Sağ tık menüsünü göster"""
        try:
            self.context_menu.tk_popup(event.x_root, event.y_root)
        finally:
            self.context_menu.grab_release()
    
    def on_double_click(self, event):
        """Çift tıklama - detayları göster"""
        self.show_details()
    
    def on_tree_double_click(self, event):
        """Treeview'da çift tıklama - tüm düzenlenebilir sütunları düzenle"""
        region = self.tree.identify("region", event.x, event.y)
        if region != "cell":
            return
        
        column = self.tree.identify_column(event.x)
        item = self.tree.identify_row(event.y)
        
        if not item:
            return
        
        col_index = int(column.replace('#', '')) - 1
        columns = ('ID', 'SİGORTALI', 'TARİH', 'MÜŞTERİ_NO', 'POLİÇE_NO', 'TÜR', 
                   'ŞİRKET', 'PLAKA', 'MARKA', 'TUTAR', 'AÇIKLAMA', 'REFERANS', 'İLETİŞİM')
        
        if col_index >= len(columns) or col_index == 0:  # ID düzenlenemez
            return
        
        col_name = columns[col_index]
        values = self.tree.item(item, 'values')
        record_id = values[0]
        current_value = values[col_index] if col_index < len(values) else ""
        
        # Düzenlenebilir sütunlar ve veritabanı alanları
        editable_cols = {
            'SİGORTALI': ('sigortali', 'entry'),
            'TARİH': ('tarih', 'entry'),
            'MÜŞTERİ_NO': ('musteri_no', 'entry'),
            'POLİÇE_NO': ('police_no', 'entry'),
            'TÜR': ('tur', 'combo_tur'),
            'ŞİRKET': ('sirket', 'combo_sirket'),
            'PLAKA': ('plaka', 'entry'),
            'MARKA': ('marka', 'entry'),
            'TUTAR': ('tutar', 'entry'),
            'AÇIKLAMA': ('aciklama', 'combo_aciklama'),
            'REFERANS': ('referans', 'entry'),
            'İLETİŞİM': ('iletisim', 'entry')
        }
        
        if col_name not in editable_cols:
            return
        
        db_field, widget_type = editable_cols[col_name]
        
        # Önceki entry'yi kapat
        if self.current_edit_entry:
            self.current_edit_entry.destroy()
            self.current_edit_entry = None
        
        # Hücre koordinatlarını al
        bbox = self.tree.bbox(item, column)
        if not bbox:
            return
        
        x, y, width, height = bbox
        
        # Widget oluştur
        if widget_type == 'combo_tur':
            self.current_edit_entry = ttk.Combobox(self.tree, 
                values=['TRAFİK', 'KASKO', 'DASK', 'EVİM', 'SAĞLIK', 'SEYAHAT', 'NAKLİYAT', 'İŞYERİ'],
                font=('Arial', 9), state='readonly')
            self.current_edit_entry.set(current_value if current_value in ['TRAFİK', 'KASKO', 'DASK', 'EVİM', 'SAĞLIK', 'SEYAHAT', 'NAKLİYAT', 'İŞYERİ'] else 'TRAFİK')
        elif widget_type == 'combo_sirket':
            self.current_edit_entry = ttk.Combobox(self.tree, 
                values=['AXA', 'ALLIANZ', 'HDI', 'RAY', 'ETHİCA', 'SOMPO', 'QUICK', 'DOĞA', 'HEPİYİ'],
                font=('Arial', 9), state='readonly')
            self.current_edit_entry.set(current_value if current_value in ['AXA', 'ALLIANZ', 'HDI', 'RAY', 'ETHİCA', 'SOMPO', 'QUICK', 'DOĞA', 'HEPİYİ'] else 'AXA')
        elif widget_type == 'combo_aciklama':
            self.current_edit_entry = ttk.Combobox(self.tree, 
                values=['YAŞAR', 'KAMİL', 'TEZCAN', 'TEZER'],
                font=('Arial', 9))
            self.current_edit_entry.set(current_value)
        else:
            self.current_edit_entry = tk.Entry(self.tree, font=('Arial', 9))
            self.current_edit_entry.insert(0, current_value)
            self.current_edit_entry.select_range(0, tk.END)
        
        self.current_edit_entry.place(x=x, y=y, width=width, height=height)
        self.current_edit_entry.focus_set()
        
        def save_edit(e=None):
            new_value = self.current_edit_entry.get().strip()
            
            if self.db_manager.update_record(record_id, **{db_field: new_value}):
                # Tablodaki değeri güncelle
                new_values = list(values)
                new_values[col_index] = new_value
                self.tree.item(item, values=new_values)
                self.log(f"✅ {col_name} güncellendi", "success")
            else:
                self.log(f"❌ {col_name} güncellenemedi", "error")
            
            if self.current_edit_entry:
                self.current_edit_entry.destroy()
                self.current_edit_entry = None
        
        def cancel_edit(e=None):
            if self.current_edit_entry:
                self.current_edit_entry.destroy()
                self.current_edit_entry = None
        
        self.current_edit_entry.bind('<Return>', save_edit)
        self.current_edit_entry.bind('<Escape>', cancel_edit)
        self.current_edit_entry.bind('<FocusOut>', save_edit)
        
        if isinstance(self.current_edit_entry, ttk.Combobox):
            self.current_edit_entry.bind('<<ComboboxSelected>>', save_edit)
    
    # =========================================================================
    # VERİ İŞLEMLERİ
    # =========================================================================
    
    def load_data(self, df=None, highlight_from_id=None):
        """Verileri yükle
        
        Args:
            df: DataFrame (opsiyonel)
            highlight_from_id: Bu ID'den sonraki kayıtlar yeşil gösterilir (opsiyonel)
        """
        # Temizle
        for item in self.tree.get_children():
            self.tree.delete(item)
        
        # Veri al
        if df is None:
            df = self.db_manager.get_all_records()
        
        if df.empty:
            self.log("ℹ️ Gösterilecek kayıt yok", "info")
            return
        
        # Debug: DataFrame boyutunu kontrol et
        print(f"DEBUG: DataFrame shape: {df.shape}, rows: {len(df)}")
        
        # Eğer highlight_from_id belirtilmediyse, session başlangıcındaki ID'yi kullan
        if highlight_from_id is None:
            highlight_from_id = self.db_manager.session_start_max_id
        
        # Ekle - tüm satırları ekle (KAYIT_TARIHI olmadan)
        added_count = 0
        try:
            for idx, row in df.iterrows():
                try:
                    values = (
                        row['ID'],
                        row['SIGORTALI'],
                        row['TARIH'],
                        row.get('MUSTERI_NO', '-'),
                        row['POLICE_NO'],
                        row['TUR'],
                        row['SIRKET'],
                        row['PLAKA'],
                        row['MARKA'],
                        row['TUTAR'],
                        row.get('ACIKLAMA', '-'),
                        row.get('REFERANS', '-'),
                        row.get('ILETISIM', '-')
                    )
                    # Yeni kayıtları tespit et (belirtilen ID'den sonra eklenenler)
                    if row['ID'] > highlight_from_id:
                        # Açık yeşil tema ile ekle
                        self.tree.insert('', tk.END, values=values, tags=('yeni_kayit',))
                    else:
                        self.tree.insert('', tk.END, values=values)
                    added_count += 1
                except Exception as e:
                    print(f"DEBUG: Satır {idx} eklenirken hata: {e}")
                    continue
        except Exception as e:
            print(f"DEBUG: iterrows hatası: {e}")
        
        print(f"DEBUG: Tabloya eklenen satır sayısı: {added_count}")
        self.log(f"✅ {len(df)} kayıt yüklendi (Scroll ile tümünü görüntüleyebilirsiniz)", "success")
        self.update_statistics()
    
    def apply_filters(self):
        """Filtreleri uygula"""
        filters = {}
        
        if self.filter_sigortali.get().strip():
            filters['sigortali'] = self.filter_sigortali.get().strip()
        
        if self.filter_police.get().strip():
            filters['police_no'] = self.filter_police.get().strip()
        
        if self.filter_tur.get() != 'Tümü':
            filters['tur'] = self.filter_tur.get()
        
        if self.filter_sirket.get() != 'Tümü':
            filters['sirket'] = self.filter_sirket.get()
        
        if self.filter_plaka.get().strip():
            filters['plaka'] = self.filter_plaka.get().strip()
        
        if filters:
            df = self.db_manager.search_records(**filters)
            self.load_data(df)
            self.log(f"🔍 Filtre uygulandı: {len(filters)} kriter", "info")
        else:
            self.load_data()
    
    def clear_filters(self):
        """Filtreleri temizle"""
        self.filter_sigortali.delete(0, tk.END)
        self.filter_police.delete(0, tk.END)
        self.filter_tur.set('Tümü')
        self.filter_sirket.set('Tümü')
        self.filter_plaka.delete(0, tk.END)
        self.load_data()
        self.log("🔄 Filtreler temizlendi", "info")
    
    def update_statistics(self):
        """İstatistikleri güncelle"""
        stats = self.db_manager.get_statistics()
        
        text = "📊 GENEL İSTATİSTİKLER\n"
        text += "=" * 35 + "\n\n"
        text += f"📄 Toplam Kayıt: {stats['total']}\n"
        text += f"📅 Bu Ay: {stats['this_month']}\n\n"
        
        text += "🏢 ŞİRKET BAZLI\n"
        text += "-" * 35 + "\n"
        for company, count in stats['company']:
            if company:
                text += f"   {company}: {count}\n"
        
        text += "\n📋 TÜR BAZLI\n"
        text += "-" * 35 + "\n"
        for type_name, count in stats['type']:
            if type_name:
                text += f"   {type_name}: {count}\n"
        
        self.stats_text.delete('1.0', tk.END)
        self.stats_text.insert('1.0', text)
    
    # =========================================================================
    # PDF İŞLEMLERİ
    # =========================================================================
    
    def process_pdf_folder(self):
        """PDF klasörü seç ve işle"""
        if not TAYYARE_AVAILABLE:
            messagebox.showerror("Hata", "Tayyare2.py modülü bulunamadı!")
            return
        
        folder = filedialog.askdirectory(title="PDF Klasörünü Seçin")
        if not folder:
            return
        
        pdf_files = [f for f in os.listdir(folder) if f.lower().endswith('.pdf')]
        
        if not pdf_files:
            messagebox.showwarning("Uyarı", "Klasörde PDF dosyası bulunamadı!")
            return
        
        # PDF işleme başlamadan önce mevcut max ID'yi kaydet
        max_id_before = self.db_manager.get_max_id()
        
        self.log(f"📂 {len(pdf_files)} PDF dosyası bulundu", "info")
        
        # İşlem paneli - Ana pencerede göster
        progress_win = tk.Frame(self.root, bg='white', relief=tk.RIDGE, borderwidth=2)
        progress_win.place(relx=0.5, rely=0.5, anchor='center', width=450, height=150)
        
        # Başlık
        title_label = tk.Label(progress_win, text="📄 PDF İşleniyor...", font=('Arial', 12, 'bold'), bg='white', fg=COLORS['primary'])
        title_label.pack(pady=10)
        
        progress_label = tk.Label(progress_win, text="İşleniyor...", font=('Arial', 10), bg='white')
        progress_label.pack(pady=10)
        
        progress_bar = ttk.Progressbar(progress_win, length=400, mode='determinate')
        progress_bar.pack(pady=10)
        
        basarili = 0
        basarisiz = 0
        processed_data = []
        duplicate_pdfs = []  # Duplicate PDF listesi
        
        for idx, pdf_file in enumerate(pdf_files):
            pdf_path = os.path.join(folder, pdf_file)
            progress_label.config(text=f"{pdf_file}...")
            progress_bar['value'] = ((idx + 1) / len(pdf_files)) * 100
            progress_win.update()
            
            try:
                # Poliçe türünü belirle
                policy_type = identify_policy_type(pdf_path)
                
                if policy_type == "BILINMIYOR":
                    self.log(f"⚠️ Bilinmeyen tür: {pdf_file}", "warning")
                    basarisiz += 1
                    continue
                
                # İşle - Tüm desteklenen türler
                parser_map = {
                    'TRAFİK_HDI': process_hdi_trafik,
                    'TRAFİK_HDI_YENI': hdi_yeni_police,
                    'TRAFİK_RAY': process_ray_trafik,
                    'TRAFİK_ETHICA': process_ethica_trafik,
                    'TRAFİK_SOMPO': process_sompo_trafik,
                    'TRAFİK_QUICK': process_quick_trafik,
                    'TRAFİK_DOĞA': process_doga_trafik,
                    'TRAFİK_HEPİYİ': process_hepiyi_trafik,
                    'TRAFİK_ALLIANZ': process_allianz_trafik,
                    'KASKO_ALLIANZ': process_allianz_kasko,
                    'SEYAHAT': process_seyahat,
                    'İŞYERİ': process_isyeri,
                    'NAKLİYAT': process_nakliyat,
                    'EVİM': process_konut,
                    'KONUT': process_konut,
                    'SAĞLIK': process_saglik,
                    'DASK': process_dask,
                    'TRAFİK': lambda p, f: process_vehicle(p, f, "TRAFİK"),
                    'KASKO': lambda p, f: process_vehicle(p, f, "KASKO")
                }
                
                parser = parser_map.get(policy_type)
                if parser:
                    data = parser(pdf_path, pdf_file)
                    if data:
                        # Eksik bilgi kontrolü - ekle ama işaretle
                        sigortali = str(data.get('SİGORTALI', '')).strip()
                        police_no = str(data.get('POLİÇE NO', '')).strip()
                        
                        eksik_bilgi = []
                        if not sigortali or sigortali == '-':
                            eksik_bilgi.append('Sigortalı')
                        if not police_no or police_no == '-':
                            eksik_bilgi.append('Poliçe No')
                        
                        if eksik_bilgi:
                            # AÇIKLAMA alanına eksik bilgi notunu ekle
                            mevcut_aciklama = data.get('AÇIKLAMA', '')
                            eksik_text = f"[EKSIK: {', '.join(eksik_bilgi)}]"
                            if mevcut_aciklama and mevcut_aciklama != '-':
                                data['AÇIKLAMA'] = f"{eksik_text} {mevcut_aciklama}"
                            else:
                                data['AÇIKLAMA'] = eksik_text
                            self.log(f"⚠️ Eksik bilgi ({', '.join(eksik_bilgi)}): {pdf_file}", "warning")
                        
                        # PDF dosya ismini duplicate kontrolü için geçici alana kaydet
                        data['_PDF_FILE'] = pdf_file
                        
                        processed_data.append(data)
                        basarili += 1
                        if not eksik_bilgi:
                            self.log(f"✅ {pdf_file} - {policy_type}", "success")
                    else:
                        self.log(f"⚠️ Veri çıkarılamadı: {pdf_file}", "warning")
                        basarisiz += 1
                else:
                    self.log(f"⚠️ Parser yok ({policy_type}): {pdf_file}", "warning")
                    basarisiz += 1
            
            except Exception as e:
                self.log(f"❌ Hata ({pdf_file}): {str(e)}", "error")
                basarisiz += 1
        
        # Veritabanına ekle ve duplicate kontrolü
        added = 0
        duplicate_count = 0
        if processed_data:
            # Önce mevcut poliçe numaralarını ve referanslarını al
            conn = sqlite3.connect(self.db_manager.db_path)
            cursor = conn.cursor()
            cursor.execute("SELECT police_no, referans FROM policeler")
            existing_policies = {row[0]: row[1] for row in cursor.fetchall()}
            conn.close()
            
            # Batch içindeki poliçe numaralarını da kontrol et (batch içi duplicate'lar için)
            batch_police_nos = {}
            
            # Duplicate olanları tespit et
            for data in processed_data:
                police_no = str(data.get('POLİÇE NO', '')).strip()
                # PDF dosya ismini geçici alandan al
                new_pdf = data.get('_PDF_FILE', 'PDF dosyası')
                
                if police_no and police_no != '-':
                    # Veritabanında var mı?
                    if police_no in existing_policies:
                        # Veritabanında mevcut - PDF ismi yerine 'DB' göster
                        duplicate_pdfs.append((new_pdf, 'Veritabanında mevcut', police_no))
                    # Batch içinde daha önce işlendi mi?
                    elif police_no in batch_police_nos:
                        first_pdf = batch_police_nos[police_no]
                        duplicate_pdfs.append((new_pdf, first_pdf, police_no))
                    else:
                        # İlk kez görüyoruz, kaydet
                        batch_police_nos[police_no] = new_pdf
            
            added = insert_police_data(self.db_manager.db_path, processed_data)
            duplicate_count = len(processed_data) - added
            if added > 0:
                self.log(f"✅ {added} yeni kayıt eklendi", "success")
            if duplicate_count > 0:
                self.log(f"⚠️ {duplicate_count} kayıt zaten mevcut (duplicate)", "warning")
                # Her duplicate için detay log
                for new_pdf, existing_ref, police_no in duplicate_pdfs:
                    if existing_ref == 'Veritabanında mevcut':
                        self.log(f"   📄 {new_pdf} - \"(Poliçe: {police_no}) eklenmedi.\"", "warning")
                    else:
                        self.log(f"   📄 {new_pdf} - \"{existing_ref} ile aynı\" eklenmedi.", "warning")
        
        progress_win.destroy()
        
        self.log(f"✅ {basarili} başarılı, ❌ {basarisiz} hatalı", "success")
        
        # Mesaj kutusu
        msg = f"📊 TARAMA SONUCU\n{'='*40}\n\n"
        msg += f"📂 Taranan PDF: {len(pdf_files)} adet\n"
        msg += f"✅ Başarıyla işlenen: {basarili} adet\n"
        
        if basarisiz > 0:
            msg += f"❌ Hatalı/İşlenemedi: {basarisiz} adet\n"
        
        msg += f"\n💾 VERİTABANI İŞLEMLERİ\n{'='*40}\n\n"
        msg += f"➕ Yeni eklenen: {added} kayıt\n"
        
        if duplicate_count > 0:
            msg += f"🔄 Zaten mevcut (duplicate): {duplicate_count} kayıt\n"
        
        messagebox.showinfo("Tamamlandı", msg)
        
        # Sadece bu işlemde eklenen kayıtları yeşil göster
        self.load_data(highlight_from_id=max_id_before)
    
    # =========================================================================
    # EXCEL İŞLEMLERİ
    # =========================================================================
    
    def open_excel_report(self):
        """Excel rapor dosyası seç ve GUI'de göster"""
        filename = filedialog.askopenfilename(
            title="Excel Rapor Dosyası Seç",
            filetypes=[("Excel files", "*.xlsx *.xls"), ("All files", "*.*")]
        )
        
        if not filename:
            return
        
        try:
            # Excel dosyasını oku
            df = pd.read_excel(filename)
            
            # Excel'deki Türkçe sütun isimlerini normalize et
            column_mapping = {
                'SIRA': 'ID',
                'SİGORTALI': 'SIGORTALI',
                'TARİH': 'TARIH',
                'MÜŞTERİ NO': 'MUSTERI_NO',
                'POLİÇE NO': 'POLICE_NO',
                'TÜR': 'TUR',
                'ŞİRKET': 'SIRKET',
                'PLAKA': 'PLAKA',
                'MARKA': 'MARKA',
                'TUTAR': 'TUTAR',
                'AÇIKLAMA': 'ACIKLAMA',
                'REFERANS': 'REFERANS',
                'İLETİŞİM': 'ILETISIM'
            }
            
            # Sütun isimlerini dönüştür
            df = df.rename(columns=column_mapping)
            
            # NaN değerlerini '-' ile değiştir
            df = df.fillna('-')
            
            # Veritabanı formatına dönüştür ve ekle
            data_list = []
            for _, row in df.iterrows():
                data = {
                    'SİGORTALI': str(row.get('SIGORTALI', '-')),
                    'TARİH': str(row.get('TARIH', '-')),
                    'MÜŞTERİ NO': str(row.get('MUSTERI_NO', '-')),
                    'POLİÇE NO': str(row.get('POLICE_NO', '-')),
                    'TÜR': str(row.get('TUR', '-')),
                    'ŞİRKET': str(row.get('SIRKET', '-')),
                    'PLAKA': str(row.get('PLAKA', '-')),
                    'MARKA': str(row.get('MARKA', '-')),
                    'TUTAR': str(row.get('TUTAR', '-')),
                    'AÇIKLAMA': str(row.get('ACIKLAMA', '-')),
                    'REFERANS': str(row.get('REFERANS', '-')),
                    'İLETİŞİM': str(row.get('ILETISIM', '-'))
                }
                data_list.append(data)
            
            # Veritabanına ekle
            if TAYYARE_AVAILABLE:
                added = insert_police_data(self.db_manager.db_path, data_list)
                duplicate_count = len(data_list) - added
                
                msg = f"✅ {added} kayıt veritabanına eklendi"
                if duplicate_count > 0:
                    msg += f"\n⚠️ {duplicate_count} kayıt zaten mevcut (atlandı)"
                
                messagebox.showinfo("Tamamlandı", msg)
                self.log(f"✅ Excel'den {added} kayıt import edildi", "success")
                
                # Veritabanından yeniden yükle
                self.load_data()
            else:
                messagebox.showerror("Hata", "Veritabanı modülü bulunamadı!")
            
        except Exception as e:
            messagebox.showerror("Hata", f"Excel dosyası okunamadı:\n{str(e)}")
            self.log(f"❌ Excel okuma hatası: {str(e)}", "error")
    
    def export_to_excel(self):
        """Excel'e aktar"""
        df = self.db_manager.get_all_records()
        
        if df.empty:
            messagebox.showwarning("Uyarı", "Dışa aktarılacak veri yok!")
            return
        
        # KAYIT_TARIHI sütununu kaldır
        if 'KAYIT_TARIHI' in df.columns:
            df = df.drop('KAYIT_TARIHI', axis=1)
        
        # Sütun isimlerini büyük harfe çevir ve sıra numarası ekle
        column_mapping = {
            'ID': 'SIRA',
            'SIGORTALI': 'SİGORTALI',
            'TARIH': 'TARİH',
            'MUSTERI_NO': 'MÜŞTERİ NO',
            'POLICE_NO': 'POLİÇE NO',
            'TUR': 'TÜR',
            'SIRKET': 'ŞİRKET',
            'PLAKA': 'PLAKA',
            'MARKA': 'MARKA',
            'TUTAR': 'TUTAR',
            'ACIKLAMA': 'AÇIKLAMA',
            'REFERANS': 'REFERANS',
            'ILETISIM': 'İLETİŞİM'
        }
        df = df.rename(columns=column_mapping)
        
        # Dosya adı - Mevcut ay
        aylar = {
            1: 'Ocak', 2: 'Şubat', 3: 'Mart', 4: 'Nisan', 5: 'Mayıs', 6: 'Haziran',
            7: 'Temmuz', 8: 'Ağustos', 9: 'Eylül', 10: 'Ekim', 11: 'Kasım', 12: 'Aralık'
        }
        now = datetime.now()
        ay_adi = aylar[now.month]
        yil = now.year
        default_filename = f"Sigorta_Kayitlari_{ay_adi}_{yil}.xlsx"
        
        filename = filedialog.asksaveasfilename(
            title="Excel Dosyası Kaydet",
            defaultextension=".xlsx",
            initialfile=default_filename,
            filetypes=[("Excel files", "*.xlsx"), ("All files", "*.*")]
        )
        
        if not filename:
            return
        
        try:
            # Excel'e yaz ve biçimlendir
            with pd.ExcelWriter(filename, engine='openpyxl') as writer:
                df.to_excel(writer, sheet_name="Kayıtlar", index=False)
                
                # Worksheet'i al
                worksheet = writer.sheets['Kayıtlar']
                
                # Sütun genişliklerini ayarla (resme göre)
                column_widths = {
                    'A': 6,   # SIRA
                    'B': 30,  # SİGORTALI
                    'C': 12,  # TARİH
                    'D': 14,  # MÜŞTERİ NO
                    'E': 14,  # POLİÇE NO
                    'F': 10,  # TÜR
                    'G': 10,  # ŞİRKET
                    'H': 12,  # PLAKA
                    'I': 18,  # MARKA
                    'J': 14,  # TUTAR
                    'K': 12,  # AÇIKLAMA
                    'L': 12,  # REFERANS
                    'M': 12   # İLETİŞİM
                }
                
                for col, width in column_widths.items():
                    worksheet.column_dimensions[col].width = width
                
                # Başlık satırını kalınlaştır ve hizala
                from openpyxl.styles import Font, Alignment
                for cell in worksheet[1]:
                    cell.font = Font(bold=True, size=11)
                    cell.alignment = Alignment(horizontal='center', vertical='center')
            
            self.log(f"✅ Excel dosyası oluşturuldu: {os.path.basename(filename)}", "success")
            messagebox.showinfo("Başarılı", f"Excel dosyası kaydedildi:\n{filename}")
        
        except Exception as e:
            self.log(f"❌ Excel hatası: {str(e)}", "error")
            messagebox.showerror("Hata", str(e))
    
    # =========================================================================
    # DİĞER İŞLEMLER
    # =========================================================================
    
    def add_inline_record(self):
        """Tabloya inline yeni satır ekle"""
        if self.editing_new_row:
            self.log("⚠️ Zaten yeni satır düzenleniyor!", "warning")
            return
        
        # Yeni satır ekle - tablonun en üstüne
        placeholder_values = (
            '🆕',           # ID
            '(Sigortalı)*',  # SİGORTALI
            '(Tarih)*',      # TARİH
            '-',             # MÜŞTERİ_NO
            '(Poliçe No)*',  # POLİÇE_NO
            '(Tür)*',        # TÜR
            '(Şirket)*',     # ŞİRKET
            '-',             # PLAKA
            '-',             # MARKA
            '(Tutar)*',      # TUTAR
            '-',             # AÇIKLAMA
            '-',             # REFERANS
            '-'              # İLETİŞİM
        )
        
        self.new_row_item = self.tree.insert('', 0, values=placeholder_values, tags=('new_row',))
        self.tree.selection_set(self.new_row_item)
        self.tree.see(self.new_row_item)
        
        self.editing_new_row = True
        self.new_row_data = {}
        
        self.log("➕ Yeni satır eklendi. Hücrelere çift tıklayarak doldurun, zorunlu alanlar (*) işaretli.", "info")
        
        # Çift tıklama eventini yeni satır için ayarla
        self.tree.bind('<Double-Button-1>', self.on_double_click_new_row)
    
    def on_double_click_new_row(self, event):
        """Yeni satırda çift tıklama - hücreyi düzenle"""
        region = self.tree.identify("region", event.x, event.y)
        if region != "cell":
            return
        
        column = self.tree.identify_column(event.x)
        item = self.tree.identify_row(event.y)
        
        if not item:
            return
        
        # Sadece yeni satırda düzenleme yap
        if item != self.new_row_item:
            return
        
        col_index = int(column.replace('#', '')) - 1
        columns = ('ID', 'SİGORTALI', 'TARİH', 'MÜŞTERİ_NO', 'POLİÇE_NO', 'TÜR', 
                   'ŞİRKET', 'PLAKA', 'MARKA', 'TUTAR', 'AÇIKLAMA', 'REFERANS', 'İLETİŞİM')
        
        if col_index >= len(columns) or col_index == 0:  # ID düzenlenemez
            return
        
        col_name = columns[col_index]
        
        # Önceki entry'yi kapat
        if self.current_edit_entry:
            self.current_edit_entry.destroy()
            self.current_edit_entry = None
        
        # Hücre koordinatları
        bbox = self.tree.bbox(item, column)
        if not bbox:
            return
        
        x, y, width, height = bbox
        
        # Widget oluştur
        if col_name == 'TÜR':
            self.current_edit_entry = ttk.Combobox(self.tree, 
                values=['TRAFİK', 'KASKO', 'DASK', 'EVİM', 'SAĞLIK', 'SEYAHAT', 'NAKLİYAT', 'İŞYERİ'],
                font=('Arial', 9), state='readonly')
            self.current_edit_entry.set('TRAFİK')
        elif col_name == 'ŞİRKET':
            self.current_edit_entry = ttk.Combobox(self.tree, 
                values=['AXA', 'ALLIANZ', 'HDI', 'RAY', 'ETHİCA', 'SOMPO', 'QUICK', 'DOĞA', 'HEPİYİ'],
                font=('Arial', 9), state='readonly')
            self.current_edit_entry.set('AXA')
        elif col_name == 'AÇIKLAMA':
            self.current_edit_entry = ttk.Combobox(self.tree, 
                values=['YAŞAR', 'KAMİL', 'TEZCAN', 'TEZER'],
                font=('Arial', 9))
        else:
            self.current_edit_entry = tk.Entry(self.tree, font=('Arial', 9))
        
        self.current_edit_entry.place(x=x, y=y, width=width, height=height)
        self.current_edit_entry.focus_set()
        
        def save_cell(e=None):
            value = self.current_edit_entry.get().strip()
            
            # Değeri kaydet
            db_field_map = {
                'SİGORTALI': 'sigortali', 'TARİH': 'tarih', 'MÜŞTERİ_NO': 'musteri_no',
                'POLİÇE_NO': 'police_no', 'TÜR': 'tur', 'ŞİRKET': 'sirket',
                'PLAKA': 'plaka', 'MARKA': 'marka', 'TUTAR': 'tutar',
                'AÇIKLAMA': 'aciklama', 'REFERANS': 'referans', 'İLETİŞİM': 'iletisim'
            }
            self.new_row_data[db_field_map[col_name]] = value
            
            # Treeview'ı güncelle
            current_values = list(self.tree.item(item, 'values'))
            current_values[col_index] = value if value else '-'
            self.tree.item(item, values=current_values)
            
            if self.current_edit_entry:
                self.current_edit_entry.destroy()
                self.current_edit_entry = None
            
            # Zorunlu alanlar dolu mu kontrol et
            self.check_and_save_inline_record()
        
        def cancel_cell(e=None):
            if self.current_edit_entry:
                self.current_edit_entry.destroy()
                self.current_edit_entry = None
        
        self.current_edit_entry.bind('<Return>', save_cell)
        self.current_edit_entry.bind('<Tab>', save_cell)
        self.current_edit_entry.bind('<Escape>', cancel_cell)
        self.current_edit_entry.bind('<FocusOut>', save_cell)
        
        if isinstance(self.current_edit_entry, ttk.Combobox):
            self.current_edit_entry.bind('<<ComboboxSelected>>', save_cell)
    
    def check_and_save_inline_record(self):
        """Zorunlu alanlar doluysa kaydı veritabanına ekle"""
        required = ['sigortali', 'tarih', 'police_no', 'tur', 'sirket', 'tutar']
        
        for field in required:
            if field not in self.new_row_data or not self.new_row_data[field]:
                return  # Henüz doldurulmamış
        
        # Tüm zorunlu alanlar dolu - kaydet
        try:
            data = [{
                'SİGORTALI': self.new_row_data.get('sigortali', '-'),
                'TARİH': self.new_row_data.get('tarih', '-'),
                'MÜŞTERİ NO': self.new_row_data.get('musteri_no', '-'),
                'POLİÇE NO': self.new_row_data.get('police_no', '-'),
                'TÜR': self.new_row_data.get('tur', '-'),
                'ŞİRKET': self.new_row_data.get('sirket', '-'),
                'PLAKA': self.new_row_data.get('plaka', '-'),
                'MARKA': self.new_row_data.get('marka', '-'),
                'TUTAR': self.new_row_data.get('tutar', '0'),
                'AÇIKLAMA': self.new_row_data.get('aciklama', '-'),
                'REFERANS': self.new_row_data.get('referans', '-'),
                'İLETİŞİM': self.new_row_data.get('iletisim', '-')
            }]
            
            if TAYYARE_AVAILABLE and insert_police_data(self.db_manager.db_path, data):
                self.log(f"✅ Yeni kayıt eklendi: {self.new_row_data.get('sigortali')}", "success")
                
                # Durumu sıfırla
                self.editing_new_row = False
                self.new_row_item = None
                self.new_row_data = {}
                
                # Çift tıklama eventini eski haline döndür
                self.tree.bind('<Double-Button-1>', self.on_tree_double_click)
                
                # Verileri yeniden yükle
                self.load_data()
                self.update_statistics()
            else:
                self.log("❌ Kayıt eklenemedi!", "error")
        except Exception as e:
            self.log(f"❌ Hata: {e}", "error")
    
    def add_manual_entry(self):
        """Manuel kayıt ekle"""
        # Manuel giriş penceresi
        entry_win = tk.Toplevel(self.root)
        entry_win.title("Yeni Kayıt Ekle")
        entry_win.geometry("500x600")
        entry_win.transient(self.root)
        entry_win.grab_set()
        
        # Alanlar
        fields = {
            'Sigortalı': tk.Entry(entry_win, font=('Arial', 10)),
            'Tarih': tk.Entry(entry_win, font=('Arial', 10)),
            'Müşteri No': tk.Entry(entry_win, font=('Arial', 10)),
            'Poliçe No': tk.Entry(entry_win, font=('Arial', 10)),
            'Tür': ttk.Combobox(entry_win, values=['TRAFİK', 'KASKO', 'DASK', 'EVİM', 'SAĞLIK', 'SEYAHAT', 'NAKLİYAT', 'İŞYERİ'], font=('Arial', 10), state='readonly'),
            'Şirket': ttk.Combobox(entry_win, values=['AXA', 'HDI', 'RAY', 'ETHİCA', 'SOMPO', 'QUICK', 'DOĞA', 'HEPİYİ'], font=('Arial', 10), state='readonly'),
            'Plaka': tk.Entry(entry_win, font=('Arial', 10)),
            'Marka': tk.Entry(entry_win, font=('Arial', 10)),
            'Tutar': tk.Entry(entry_win, font=('Arial', 10)),
            'Açıklama': ttk.Combobox(entry_win, values=['YAŞAR', 'KAMİL', 'TEZCAN', 'TEZER'], font=('Arial', 10)),
            'Referans': tk.Text(entry_win, height=2, font=('Arial', 10)),
            'İletişim': tk.Text(entry_win, height=2, font=('Arial', 10))
        }
        
        row = 0
        for label, widget in fields.items():
            tk.Label(entry_win, text=f"{label}:", font=('Arial', 9, 'bold')).grid(row=row, column=0, sticky='w', padx=10, pady=5)
            widget.grid(row=row, column=1, sticky='ew', padx=10, pady=5)
            row += 1
        
        entry_win.grid_columnconfigure(1, weight=1)
        
        def save_entry():
            data = [{
                'SİGORTALI': fields['Sigortalı'].get(),
                'TARİH': fields['Tarih'].get(),
                'MÜŞTERİ NO': fields['Müşteri No'].get(),
                'POLİÇE NO': fields['Poliçe No'].get(),
                'TÜR': fields['Tür'].get(),
                'ŞİRKET': fields['Şirket'].get(),
                'PLAKA': fields['Plaka'].get(),
                'MARKA': fields['Marka'].get(),
                'TUTAR': fields['Tutar'].get(),
                'AÇIKLAMA': fields['Açıklama'].get(),
                'REFERANS': fields['Referans'].get('1.0', tk.END).strip(),
                'İLETİŞİM': fields['İletişim'].get('1.0', tk.END).strip()
            }]
            
            if insert_police_data(self.db_manager.db_path, data):
                self.log("✅ Yeni kayıt eklendi", "success")
                self.load_data()
                entry_win.destroy()
            else:
                messagebox.showerror("Hata", "Kayıt eklenemedi!")
        
        btn_frame = tk.Frame(entry_win)
        btn_frame.grid(row=row, column=0, columnspan=2, pady=20)
        
        tk.Button(btn_frame, text="💾 Kaydet", command=save_entry, font=('Arial', 10, 'bold'), bg=COLORS['success'], fg='white', padx=20, pady=8).pack(side=tk.LEFT, padx=5)
        tk.Button(btn_frame, text="❌ İptal", command=entry_win.destroy, font=('Arial', 10, 'bold'), bg=COLORS['danger'], fg='white', padx=20, pady=8).pack(side=tk.LEFT, padx=5)
    
    def edit_selected(self):
        """Seçili kaydı düzenle"""
        selection = self.tree.selection()
        if not selection:
            messagebox.showwarning("Uyarı", "Lütfen bir kayıt seçin!")
            return
        
        if len(selection) > 1:
            messagebox.showwarning("Uyarı", "Lütfen tek bir kayıt seçin!")
            return
        
        # Kayıt bilgilerini al
        values = self.tree.item(selection[0], 'values')
        record_id = values[0]
        
        # Düzenleme özelliği devre dışı bırakıldı
        return
    
    def delete_selected(self):
        """Seçili kayıtları sil"""
        selection = self.tree.selection()
        if not selection:
            messagebox.showwarning("Uyarı", "Lütfen kayıt seçin!")
            return
        
        if messagebox.askyesno("Onay", f"{len(selection)} kayıt silinecek. Emin misiniz?"):
            for item in selection:
                values = self.tree.item(item, 'values')
                record_id = values[0]
                self.db_manager.delete_record(record_id)
            
            self.log(f"🗑️ {len(selection)} kayıt silindi", "warning")
            self.load_data()
    
    def delete_all_records(self):
        """Tüm kayıtları sil"""
        # Önce toplam kayıt sayısını al
        df = self.db_manager.get_all_records()
        total_count = len(df)
        
        if total_count == 0:
            messagebox.showinfo("Bilgi", "Silinecek kayıt yok!")
            return
        
        # İlk onay
        response = messagebox.askyesno(
            "⚠️ TEHLİKELİ İŞLEM",
            f"TÜM {total_count} KAYIT SİLİNECEK!\n\nBu işlem geri alınamaz.\n\nDevam etmek istediğinizden emin misiniz?",
            icon='warning'
        )
        
        if not response:
            return
        
        # İkinci onay (çift kontrol)
        response2 = messagebox.askyesno(
            "⚠️ SON ONAY",
            f"Tekrar soruyoruz:\n\n{total_count} kayıt kalıcı olarak silinecek!\n\nGerçekten devam etmek istiyor musunuz?",
            icon='warning'
        )
        
        if not response2:
            self.log("❌ İşlem iptal edildi", "info")
            return
        
        try:
            # Tüm kayıtları sil
            conn = sqlite3.connect(self.db_manager.db_path)
            cursor = conn.cursor()
            cursor.execute("DELETE FROM policeler")
            cursor.execute("DELETE FROM sqlite_sequence WHERE name='policeler'")  # ID sıfırla
            conn.commit()
            conn.close()
            
            self.log(f"🗑️ TÜM KAYITLAR SİLİNDİ: {total_count} kayıt", "warning")
            self.load_data()
            messagebox.showinfo("Tamamlandı", f"✅ {total_count} kayıt başarıyla silindi!")
        
        except Exception as e:
            self.log(f"❌ Silme hatası: {str(e)}", "error")
            messagebox.showerror("Hata", f"Kayıtlar silinirken hata oluştu:\n{str(e)}")
    
    def show_details(self):
        """Kayıt detaylarını göster"""
        selection = self.tree.selection()
        if not selection:
            messagebox.showwarning("Uyarı", "Lütfen bir kayıt seçin!")
            return
        
        if len(selection) > 1:
            messagebox.showwarning("Uyarı", "Lütfen tek bir kayıt seçin!")
            return
        
        # Detay gösterme devre dışı bırakıldı
        return
        
        values = self.tree.item(selection[0], 'values')
        
        details = f"""
📋 KAYIT DETAYLARI

ID: {values[0]}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

👤 Sigortalı: {values[1]}
📅 Tarih: {values[2]}
📄 Poliçe No: {values[3]}
📋 Tür: {values[4]}
🏢 Şirket: {values[5]}
🚗 Plaka: {values[6]}
🏭 Marka: {values[7]}
💰 Tutar: {values[8]}
⏰ Kayıt Tarihi: {values[9]}
        """
        
        messagebox.showinfo("Kayıt Detayları", details)
    
    def sort_column(self, col):
        """Sütuna göre sırala"""
        # Basit sıralama mantığı eklenebilir
        pass
    
    def backup_database(self):
        """Veritabanını yedekle"""
        import shutil
        
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        backup_name = f"policeler_backup_{timestamp}.db"
        
        filename = filedialog.asksaveasfilename(
            title="Veritabanı Yedekle",
            defaultextension=".db",
            initialfile=backup_name,
            filetypes=[("Database files", "*.db"), ("All files", "*.*")]
        )
        
        if filename:
            try:
                shutil.copy2(self.db_manager.db_path, filename)
                self.log(f"✅ Veritabanı yedeklendi: {os.path.basename(filename)}", "success")
                messagebox.showinfo("Başarılı", f"Veritabanı yedeklendi:\n{filename}")
            except Exception as e:
                self.log(f"❌ Yedekleme hatası: {str(e)}", "error")
                messagebox.showerror("Hata", str(e))
    
    def show_company_report(self):
        """Şirket bazlı rapor göster"""
        stats = self.db_manager.get_statistics()
        
        report = "📊 ŞİRKET BAZLI RAPOR\n\n"
        for company, count in stats['company']:
            if company:
                report += f"{company}: {count} kayıt\n"
        
        messagebox.showinfo("Şirket Bazlı Rapor", report)
    
    def show_type_report(self):
        """Tür bazlı rapor göster"""
        stats = self.db_manager.get_statistics()
        
        report = "📊 TÜR BAZLI RAPOR\n\n"
        for type_name, count in stats['type']:
            if type_name:
                report += f"{type_name}: {count} kayıt\n"
        
        messagebox.showinfo("Tür Bazlı Rapor", report)
    
    def show_monthly_summary(self):
        """Aylık özet göster"""
        stats = self.db_manager.get_statistics()
        
        summary = f"""
📊 AYLIK ÖZET

Bu Ay: {stats['this_month']} kayıt
Toplam: {stats['total']} kayıt
        """
        
        messagebox.showinfo("Aylık Özet", summary)
    
    def log(self, message, level="info"):
        """Log ekle"""
        timestamp = datetime.now().strftime('%H:%M:%S')
        log_entry = f"[{timestamp}] {message}\n"
        
        self.log_text.insert(tk.END, log_entry, level)
        self.log_text.see(tk.END)
        
        # Durum çubuğu
        self.status_bar.config(text=message)
    
    def clear_logs(self):
        """Logları temizle"""
        self.log_text.delete('1.0', tk.END)
        self.log("🗑️ Loglar temizlendi", "info")
    
    def show_help(self):
        """Yardım göster"""
        help_text = """
📊 SİGORTA RAPOR VE KAYIT YÖNETİMİ - KULLANIM KILAVUZU

1. PDF İŞLEME
   - "📂 PDF Klasörü İşle" butonuna tıklayın
   - PDF'lerin bulunduğu klasörü seçin
   - Otomatik işleme ve veritabanına kayıt başlayacaktır

2. FİLTRELEME VE ARAMA
   - Sol panelden filtre kriterlerini girin
   - "🔍 ARA" butonuna tıklayın
   - Sonuçlar tabloda görünür

3. EXCEL RAPORU
   - "💾 Excel'e Aktar" butonuna tıklayın
   - Kayıt yerini seçin
   - Tüm veriler Excel formatında kaydedilir

4. KAYIT YÖNETİMİ
   - Yeni kayıt eklemek için "➕ Yeni Kayıt Ekle"
   - Kayıt düzenlemek için çift tıklayın veya sağ tık menüsünü kullanın
   - Kayıt silmek için seçip "🗑️ Sil" seçeneğini kullanın

5. VERİTABANI
   - Tüm veriler SQLite veritabanında saklanır
   - "Veritabanı Yedekle" menüsünden yedek alabilirsiniz

İyi çalışmalar!
        """
        
        help_win = tk.Toplevel(self.root)
        help_win.title("Yardım")
        help_win.geometry("700x600")
        
        text = tk.Text(help_win, wrap=tk.WORD, font=('Consolas', 10))
        text.pack(fill=tk.BOTH, expand=True, padx=10, pady=10)
        text.insert('1.0', help_text)
        text.config(state='disabled')
    
    def show_about(self):
        """Hakkında"""
        about_text = """
📊 Sigorta Rapor ve Kayıt Yönetimi
Versiyon: 1.0

✨ Özellikler:
• Otomatik PDF okuma ve veri çıkarma
• SQLite veritabanı entegrasyonu
• Gelişmiş filtreleme ve arama
• Excel raporlama
• İstatistik ve analiz
• Modern kullanıcı arayüzü

🏢 Desteklenen Şirketler:
HDI, RAY, ETHICA, SOMPO, QUICK, DOĞA, HEPİYİ

👨‍💻 Geliştirme: 2026
        """
        messagebox.showinfo("Hakkında", about_text)


# ==============================================================================
# ANA PROGRAM
# ==============================================================================

if __name__ == "__main__":
    root = tk.Tk()
    app = RaporGUI(root)
    root.mainloop()
